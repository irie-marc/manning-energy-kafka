import energy.avro.BatteryState;
import energy.avro.DeviceMessageMetadata;
import jakarta.ws.rs.*;
import org.apache.kafka.common.protocol.types.Field;
import repository.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("battery")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class BatteryStateResource {

  public static final String DEVICE_ID = "deviceId";
  private final BatteryStateRepository repository;
  private final MetadataRepository metadataRepository;

  public BatteryStateResource(
      BatteryStateRepository repository, MetadataRepository metadataRepository) {
    this.repository = repository;
    this.metadataRepository = metadataRepository;
  }

  @GET
  public List<BatteryStateDaoImpl> getBatteryStates(@QueryParam(DEVICE_ID) String deviceId) {
    return repository.findBatteryStatesById(UUID.fromString(deviceId));
  }

  @GET
  @Path("/metadata")
  public List<DeviceMetadataDAOImpl> getMetadata(@QueryParam(DEVICE_ID) String deviceId) {
    List<DeviceMetadataDAOImpl> deviceMessageMetadata = metadataRepository.findDeviceMetadataByDeviceId(UUID.fromString(deviceId));
    return deviceMessageMetadata;
  }
}
