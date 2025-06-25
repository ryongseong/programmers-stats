package properties;

import lombok.Getter;

@Getter
public class EnvProperties {

    private static final EnvProperties envProperties = new EnvProperties();

    private String id;
    private String password;

    private EnvProperties() {
        this.id = System.getenv("PROGRAMMERS_TOKEN_ID");
        this.password = System.getenv("PROGRAMMERS_TOKEN_PW");
    }

    public static EnvProperties getEnvProperties() {
        return envProperties;
    }

} 