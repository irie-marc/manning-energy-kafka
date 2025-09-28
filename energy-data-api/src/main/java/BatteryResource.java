import energy.avro.BatteryState;
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
import java.util.UUID;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@Path("battery/{id}")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class BatteryResource {

    final String ID_PARAM = "id";
    private final String BATTERY_STATE_TOPIC = "battery-event";

    private final Logger LOGGER = LoggerFactory.getLogger(BatteryResource.class);
    private final Schema schema = BatteryState.getClassSchema();
    private final SpecificDatumReader<BatteryState> reader = new SpecificDatumReader<>(BatteryState.class);
    private final Producer<String, BatteryState> producer;


    public BatteryResource(KafkaProducer<String, BatteryState> producer) {
        this.producer = producer;
    }

    @POST
    public Response event(@PathParam(ID_PARAM) String id, String content) {
        try {
            Decoder decoder = DecoderFactory.get().jsonDecoder(schema, content);
            BatteryState batteryState = reader.read(null, decoder);
            LOGGER.info(String.format("Deserialized %s", batteryState));
            producer.send(new ProducerRecord<>(BATTERY_STATE_TOPIC, UUID.randomUUID().toString(), batteryState));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Response.accepted().build();
    }

}