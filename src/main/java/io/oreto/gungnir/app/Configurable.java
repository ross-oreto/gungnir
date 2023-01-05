package io.oreto.gungnir.app;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Objects;

public interface Configurable {

    /**
     * Load the config files in this order of priority: (give gungnir.profiles = ['dev'])
     * 1. System properties
     * 2. application-dev.conf (would just be application.conf when profiles are empty)
     * 3. reference.conf
     * <p>
     * system properties can be used to force a different config source (e.g. from command line -Dconfig.file=path/to/config-file):
     *  - config.resource specifies a resource name - not a basename, i.e. application.conf not application
     *  - config.file specifies a filesystem path, again it should include the extension, not be a basename
     *  - config.url specifies a URL
     * Note: you need to pass -Dconfig.file=path/to/config-file before the jar itself,
     * e.g. java -Dconfig.file=path/to/config-file.conf -jar path/to/jar-file.jar. Same applies for -Dconfig.resource=config-file.conf
     *  </p>
     * @param profiles The current application profiles
     * @return The merged config properties
     */
    static Config loadConfig(String... profiles) {
        final String baseName = "application";
        Config conf;
        if (profiles.length == 0
                || Objects.nonNull(System.getProperty("config.resource"))
                || Objects.nonNull(System.getProperty("config.file"))
                || Objects.nonNull(System.getProperty("config.url"))) {
            conf = ConfigFactory.load();
        } else {
            conf = ConfigFactory.load(String.format("%s-%s", baseName, profiles[0]));
            for (int i = 1; i < profiles.length; i++) {
                conf = conf.withFallback(ConfigFactory.parseResourcesAnySyntax(String.format("%s-%s", baseName, profiles[i])));
            }
        }
        return conf.resolve();
    }

    /**
     * Provide the configuration properties
     * @return The config
     */
    Config conf();
}
