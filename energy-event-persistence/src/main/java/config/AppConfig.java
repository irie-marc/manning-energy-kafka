package config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import io.dropwizard.db.DataSourceFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class AppConfig extends Configuration {
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();


    @Valid
    @NotNull
    private KafkaConfig kafkaConfigFactory = new KafkaConfig();



    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }


    @JsonProperty
    public KafkaConfig getKafkaConfigFactory() {
        return kafkaConfigFactory;
    }

    @JsonProperty("kafka")
    public void setKafkaConfigFactory(KafkaConfig kafkaConfigFactory) {
        this.kafkaConfigFactory = kafkaConfigFactory;
    }
}
