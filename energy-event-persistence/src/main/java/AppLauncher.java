import config.AppConfig;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.BatteryStateRepository;
import repository.JdbiProvider;
import repository.MetadataRepository;

public class AppLauncher extends Application<AppConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppLauncher.class);

    public static void main(String[] argv) {
        try {
            new AppLauncher().run(argv);
        } catch (Exception e) {
            LOGGER.error("App crash", e);
        }
    }

    @Override
    public void run(AppConfig appConfig, Environment environment) throws Exception {
        Jdbi jdbi = JdbiProvider.provideJdbi(environment, appConfig.getDataSourceFactory());
        BatteryStateRepository repo = new BatteryStateRepository(jdbi);
        MetadataRepository metadataRepository = new MetadataRepository(jdbi);
        BatteryStateResource batteryStateResource = new BatteryStateResource(repo, metadataRepository);
        environment.jersey().register(batteryStateResource);
    }

    @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {
        bootstrap.addCommand(new KafkaListenerCommand(this, "kafka", "listener"));
    }
}
