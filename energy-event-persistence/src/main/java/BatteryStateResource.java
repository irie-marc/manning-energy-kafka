import energy.avro.BatteryState;
import jakarta.ws.rs.*;
import repository.BatteryStateDaoImpl;
import repository.BatteryStateRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("battery")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class BatteryStateResource {

    private final BatteryStateRepository repository;

    public BatteryStateResource(BatteryStateRepository repository) {
        this.repository = repository;
    }

    @GET
    public List<BatteryStateDaoImpl> getBatteryStates(@QueryParam("deviceId") String deviceId) {
        return repository.findBatteryStatesById(UUID.fromString(deviceId));
    }
}
