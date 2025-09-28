package config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class KafkaConfig {

    @NotEmpty
    private String schemaRegistryUrl;
    @NotEmpty
    private String bootStrapServerUrl;
    @NotEmpty
    private String applicationId;

    @JsonProperty
    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    @JsonProperty
    public void setSchemaRegistryUrl(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    @JsonProperty
    public String getBootStrapServerUrl() {
        return bootStrapServerUrl;
    }

    @JsonProperty
    public void setBootStrapServerUrl(String bootStrapServerUrl) {
        this.bootStrapServerUrl = bootStrapServerUrl;
    }

    @JsonProperty
    public String getApplicationId() {
        return applicationId;
    }

    @JsonProperty
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

}
