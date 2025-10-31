import com.fasterxml.jackson.databind.JsonDeserializer;
import config.AppConfig;
import config.KafkaConfig;
import energy.avro.BatteryState;
import energy.avro.DeviceMessageMetadata;
import energy.avro.RawDeviceEvent;
import energy.avro.UnparsedEventData;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.dropwizard.core.Application;
import io.dropwizard.core.cli.EnvironmentCommand;
import io.dropwizard.core.setup.Environment;
import kafka.KafkaStreamsConsumer;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.BatteryStateRepository;
import repository.JdbiProvider;
import repository.MetadataRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class KafkaListenerCommand extends EnvironmentCommand<AppConfig> {

    public static final String TOPIC_BATTERY_EVENT_RAW = "battery-event-raw";
    public static final String TOPIC_DEAD_LETTER_QUEUE = "dead-letter-queue";
    private static final int MAX_PARSE_TIME_MS = 400;
    private static final ExecutorService executor = Executors.newFixedThreadPool(8);
    public static final String TOPIC_BATTERY_EVENT = "battery-event";
    public static final String TOPIC_SLOW_EVENT = "slow-event";
    private final String LARGE_RECORDS_TOPIC = "battery-event-large";
    public static final String DEVICE_MESSAGE_METADATA = "device-message-metadata";


    private final SpecificDatumReader<BatteryState> reader = new SpecificDatumReader<>(BatteryState.class);
    private final SpecificDatumReader<RawDeviceEvent> rawDeviceEventReader = new SpecificDatumReader<>(RawDeviceEvent.class);
    private final Logger LOGGER = LoggerFactory.getLogger(KafkaListenerCommand.class);
    private BatteryStateRepository batteryStateRepository;
    private MetadataRepository metadataRepository;


    protected KafkaListenerCommand(Application<AppConfig> app, String name, String description) {
        super(app, name, description);

    }

    private static void addShutdownHook(KafkaStreams streams) {
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
            }
        });
    }


    private void parseOrSendToSlowLaneEventHandler(KStream<String, GenericData.Record> stream) {
        KStream<String, BatteryStateParseResult>[] branches = stream.map((key, value) -> {
                    try {
                        Decoder decoder = DecoderFactory.get().jsonDecoder(RawDeviceEvent.getClassSchema(), value.toString());
                        RawDeviceEvent rawDeviceEvent = rawDeviceEventReader.read(null, decoder);
                        BatteryState batteryState = tryParseWithinTime(rawDeviceEvent, MAX_PARSE_TIME_MS);

                        BatteryStateParseResult batteryStateParseResult = new BatteryStateParseResult(batteryState, batteryState == null, value.toString());
                        return KeyValue.pair(key, batteryStateParseResult);
                    } catch (Exception e) {
                        return KeyValue.pair(key, new BatteryStateParseResult(null, false, value.toString()));
                    }
                }
        ).branch((key, value) -> !value.isSlow(), (key, value) -> true);
        branches[0].map((key, value) -> KeyValue.pair(key, value.getParsedValue())).to(TOPIC_BATTERY_EVENT);
        branches[1].map((key, value) -> {
            UnparsedEventData data = new UnparsedEventData();
            data.setData(value.getRawValue());
            return KeyValue.pair(key, data);
        }).to(TOPIC_SLOW_EVENT);
    }

    private void storageHandler(KStream<String, GenericData.Record> stream) {
        stream.map((key, value) -> {
            try {
                BatteryState batteryState = parseRawEvent(value.toString());
                LOGGER.info(String.format("Storing event. Device id: %s", batteryState.getDeviceId().toString()));
                batteryStateRepository.saveBatteryState(
                        UUID.fromString(batteryState.getDeviceId().toString()),
                        batteryState.getChargingSource().toString(),
                        batteryState.getCharging(),
                        batteryState.getCurrentCapacity()
                );
            } catch (IOException e) {
                LOGGER.error("Failed to save event");
            }


            return KeyValue.pair(key, value);

        });
    }

    private void deadLetterQueueHandler(KStream<String, String> stream) {
        stream.map((key, value) -> {
            LOGGER.info(String.format("Deadletter Queue event processed, %s", value));
            return KeyValue.pair(key, value);
        });
    }

    private void slowEventHandler(KStream<String, GenericData.Record> stream) {
        stream.map((key, value) -> {
            try {
                LOGGER.info(String.format("Processed slow event ", key));
                Decoder unparseDEventDeocder = DecoderFactory.get().jsonDecoder(UnparsedEventData.getClassSchema(), value.toString());
                SpecificDatumReader<UnparsedEventData> reader = new SpecificDatumReader<>(UnparsedEventData.class);
                UnparsedEventData data = reader.read(null, unparseDEventDeocder);
                Decoder decoder = DecoderFactory.get().jsonDecoder(RawDeviceEvent.getClassSchema(), data.data.toString());
                RawDeviceEvent rawDeviceEvent = rawDeviceEventReader.read(null, decoder);
                return KeyValue.pair(key, parseRawEvent(StandardCharsets.UTF_8.decode(rawDeviceEvent.getData()).toString()));
            } catch (IOException e) {
                LOGGER.error("Failed to parse event");
                return KeyValue.pair(key, null);
            }
        }).filterNot((key, value) -> value == null).to(TOPIC_BATTERY_EVENT);
    }

    private void messageMetadataHandler(KStream<String, GenericRecord> stream) {
        stream.map((key, value) -> {
            try {
                LOGGER.info(String.format("Received metadata event %s", value.toString()));
                DeviceMessageMetadata pojo =
                        (DeviceMessageMetadata) SpecificData.get().deepCopy(
                                DeviceMessageMetadata.getClassSchema(),
                                value
                        );
                metadataRepository.saveMetadata(pojo);
                return KeyValue.pair(key, value);
            } catch (Exception e) {
                LOGGER.error("Runtime exception while parsing", e);
                return KeyValue.pair(key, null);
            }
        });
    }

    private void fakeS3LargeEventHandler(KStream<String, GenericData.Record> stream) {
        stream.foreach((key, value) -> {
            LOGGER.info(String.format("Received large event %s but not sending to S3", key));
        });
    }


    @Override
    protected void run(Environment environment, Namespace namespace, AppConfig configuration) throws Exception {
        Jdbi jdbi = JdbiProvider.provideJdbi(environment, configuration.getDataSourceFactory());
        this.batteryStateRepository = new BatteryStateRepository(jdbi);
        this.metadataRepository = new MetadataRepository(jdbi);
        KafkaConfig kafkaConfig = configuration.getKafkaConfigFactory();
        KafkaStreamsConsumer consumer = createKafkaStreamsConsumer(kafkaConfig);
        KStream<String, GenericData.Record> rawEventStream = consumer.getBuilder().stream(TOPIC_BATTERY_EVENT_RAW);
        KStream<String, GenericData.Record> largeEventStream = consumer.getBuilder().stream(LARGE_RECORDS_TOPIC);
        KStream<String, String> streamDeadLetter = consumer.getBuilder().stream(TOPIC_DEAD_LETTER_QUEUE);
        KStream<String, GenericData.Record> streamCanonical = consumer.getBuilder().stream(TOPIC_BATTERY_EVENT);
        KStream<String, GenericData.Record> slowStream = consumer.getBuilder().stream(TOPIC_SLOW_EVENT);
        KStream<String, GenericRecord> deviceMessageMetadataStream = consumer.getBuilder().stream(DEVICE_MESSAGE_METADATA);
        parseOrSendToSlowLaneEventHandler(rawEventStream);
        fakeS3LargeEventHandler(largeEventStream);
        deadLetterQueueHandler(streamDeadLetter);
        storageHandler(streamCanonical);
        slowEventHandler(slowStream);
        messageMetadataHandler(deviceMessageMetadataStream);
        KafkaStreams streams = consumer.start();
        addShutdownHook(streams);
        try {
            streams.start();
        } catch (Throwable e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private BatteryState tryParseWithinTime(RawDeviceEvent raw, long timeoutMs) throws Exception {
        Future<BatteryState> future = executor.submit(() -> parseRawEvent(StandardCharsets.UTF_8.decode(raw.getData()).toString()));
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return null;
        }
    }

    private BatteryState parseRawEvent(String rawDeviceEvent) throws IOException {
        Decoder batteryStateDecoder = DecoderFactory.get().jsonDecoder(BatteryState.getClassSchema(), rawDeviceEvent);
        return this.reader.read(null, batteryStateDecoder);
    }

    private KafkaStreamsConsumer createKafkaStreamsConsumer(KafkaConfig kafkaConfig) {
        return new KafkaStreamsConsumer(
                kafkaConfig.getSchemaRegistryUrl(),
                kafkaConfig.getBootStrapServerUrl(),
                kafkaConfig.getApplicationId());
    }
}
