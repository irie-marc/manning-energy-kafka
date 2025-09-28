import config.AppConfig;
import config.KafkaConfig;
import energy.avro.BatteryState;
import io.dropwizard.core.Application;
import io.dropwizard.core.cli.EnvironmentCommand;
import io.dropwizard.core.setup.Environment;
import kafka.KafkaStreamsConsumer;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.avro.generic.GenericData;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.BatteryStateRepository;
import repository.JdbiProvider;

import java.io.IOException;
import java.util.UUID;

public class KafkaListenerCommand extends EnvironmentCommand<AppConfig> {

    private final SpecificDatumReader<BatteryState> reader = new SpecificDatumReader<>(BatteryState.class);
    private final Logger LOGGER = LoggerFactory.getLogger(KafkaListenerCommand.class);
    private BatteryStateRepository batteryStateRepository;


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


    private void streamEventHandler(KStream<String, GenericData.Record> stream) {
        stream.map((key, value) -> {
            try {
                Decoder decoder = DecoderFactory.get().jsonDecoder(value.getSchema(), value.toString());
                BatteryState batteryState = reader.read(null, decoder);
                LOGGER.info(String.format("Deserialized %s", value));
                LOGGER.info(String.format("Device id: %s", batteryState.getDeviceId().toString()));
                batteryStateRepository.saveBatteryState(
                        UUID.fromString(batteryState.getDeviceId().toString()),
                        batteryState.getChargingSource().toString(),
                        batteryState.getCharging(),
                        batteryState.getCurrentCapacity()
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return KeyValue.pair(key, value);

        });
    }


    @Override
    protected void run(Environment environment, Namespace namespace, AppConfig configuration) throws Exception {
        Jdbi jdbi = JdbiProvider.provideJdbi(environment, configuration.getDataSourceFactory());
        this.batteryStateRepository = new BatteryStateRepository(jdbi);
        KafkaConfig kafkaConfig = configuration.getKafkaConfigFactory();
        KafkaStreamsConsumer consumer = createKafkaStreamsConsumer(kafkaConfig);
        KStream<String, GenericData.Record> stream = consumer.stream("battery-event");
        streamEventHandler(stream);
        KafkaStreams streams = consumer.start();
        addShutdownHook(streams);
        try {
            streams.start();
        } catch (Throwable e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private KafkaStreamsConsumer createKafkaStreamsConsumer(KafkaConfig kafkaConfig) {
        return new KafkaStreamsConsumer(
                kafkaConfig.getSchemaRegistryUrl(),
                kafkaConfig.getBootStrapServerUrl(),
                kafkaConfig.getApplicationId());
    }
}
