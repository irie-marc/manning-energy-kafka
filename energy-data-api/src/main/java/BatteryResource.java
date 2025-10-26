import energy.avro.BatteryState;
import energy.avro.RawDeviceEvent;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@Path("battery/{id}")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class BatteryResource {

    public static final int MINIMUM_BYTES_LARGE_EVENT = 2000;
    final String ID_PARAM = "id";
    private final String BATTERY_STATE_TOPIC_RAW = "battery-event-raw";
    private final String LARGE_RECORDS_TOPIC = "battery-event-large";

    private final Logger LOGGER = LoggerFactory.getLogger(BatteryResource.class);
    private final Schema schema = BatteryState.getClassSchema();
    private final SpecificDatumReader<BatteryState> reader = new SpecificDatumReader<>(BatteryState.class);
    private final Producer<String, RawDeviceEvent> producer;


    public BatteryResource(KafkaProducer<String, RawDeviceEvent> producer) {
        this.producer = producer;
    }

    @POST
    public Response event(@PathParam(ID_PARAM) String id, String content) {
        try {
            Decoder decoder = DecoderFactory.get().jsonDecoder(schema, content);
            BatteryState batteryState = reader.read(null, decoder);
            LOGGER.info(String.format("Deserialized %s", batteryState));
            if (content.getBytes(StandardCharsets.UTF_8).length >= MINIMUM_BYTES_LARGE_EVENT) {
                producer.send(new ProducerRecord<>(
                        LARGE_RECORDS_TOPIC,
                        UUID.randomUUID().toString(),
                        new RawDeviceEvent(
                                Instant.now().getEpochSecond(),
                                ByteBuffer.wrap(batteryState.toString().getBytes(StandardCharsets.UTF_8))
                        )));
            }
            producer.send(new ProducerRecord<>(
                    BATTERY_STATE_TOPIC_RAW,
                            UUID.randomUUID().toString(),
                            new RawDeviceEvent(
                                    Instant.now().getEpochSecond(),
                                    ByteBuffer.wrap(batteryState.toString().getBytes(StandardCharsets.UTF_8))
                            )
                    )
            );
        } catch (IOException e) {
            producer.send(new ProducerRecord<>("dead-letter-queue", UUID.randomUUID().toString(), new RawDeviceEvent(
                    Instant.now().getEpochSecond(),
                    ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8))
            )));
        }

        return Response.accepted().build();
    }

}