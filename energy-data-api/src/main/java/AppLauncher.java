import energy.avro.BatteryState;
import energy.avro.DeviceMessageMetadata;
import energy.avro.RawDeviceEvent;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class AppLauncher extends Application<DeviceEventApplicationConfig> {

    public static void main(String[] args) throws Exception {
        new AppLauncher().run(args);
    }

    @Override
    public String getName() {
        return "energy-data-api";
    }

    @Override
    public void initialize(Bootstrap<DeviceEventApplicationConfig> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(DeviceEventApplicationConfig deviceEventApplicationConfig, Environment environment) throws Exception {
        Properties settings = new Properties();
        settings.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        settings.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        settings.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        settings.put("schema.registry.url", "http://localhost:8090");


        KafkaProducer<String, RawDeviceEvent> producer = new KafkaProducer<>(settings);
        KafkaProducer<String, DeviceMessageMetadata> metadataKafkaProducer = new KafkaProducer<>(settings);
        Runtime.getRuntime().addShutdownHook(new Thread("shutdown-hook") {
            @Override
            public void run() {
                producer.close();
                metadataKafkaProducer.close();
            }
        });

        final BatteryResource resource = new BatteryResource(producer, metadataKafkaProducer);
        environment.jersey().register(resource);
    }
}
