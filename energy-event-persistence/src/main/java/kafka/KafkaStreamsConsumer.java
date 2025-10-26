package kafka;

import energy.avro.BatteryState;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

public class KafkaStreamsConsumer {
    private final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsConsumer.class);
    private final StreamsBuilder builder = new StreamsBuilder();

    private final Properties properties;


    public KafkaStreamsConsumer(String schemaRegistryUrl,
                                String bootStrapServerUrl,
                                String applicationId) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServerUrl);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        this.properties = props;
    }

    public StreamsBuilder getBuilder() {
        return this.builder;
    }

    public KafkaStreams start() {
        return createKafkaStream(this.properties);
    }


    private KafkaStreams createKafkaStream(Properties props) {
        final Topology topology = this.builder.build();
        return new KafkaStreams(topology, props);
    }
}
