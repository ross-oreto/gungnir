package io.oreto.gungnir.app;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.oreto.gungnir.error.ContextFail;
import io.oreto.gungnir.route.RouteInfo;
import io.oreto.gungnir.route.Router;
import io.oreto.gungnir.security.ContextUser;
import io.oreto.gungnir.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;


/**
 * Encapsulates a set of {@link Router routing} rules and related logic.
 * <p>
 * Instance can be assigned to the {@link Router routing} using
 * {@link io.oreto.gungnir.app.Gungnir#register(Service...)} method.
 */
public abstract class Service implements IEnvironment, Configurable, ContextUser, ContextFail {
    Gungnir gungnir;
    private final String name;

    protected final Logger log;

    public Service() {
        this.log = LoggerFactory.getLogger(this.getClass());
        this.name = getClass().getName();
    }

    final void setGungnir(Gungnir gungnir) {
        this.gungnir = gungnir;
        routing(gungnir);
    }

    /**
     * Represents what should be a unique name/id of this service
     * defaults to fully qualified class name
     * @return The service name/id
     */
    public String name() {
        return name;
    }

    /**
     * Define this service by providing routing rules, handlers, and authorization
     * @param router The server to provide routes for
     */
    public abstract void routing(Router router);

    /**
     * Return all the routes registered by this service
     * @return A list of routes
     */
    protected Collection<RouteInfo> getRoutes() {
        return gungnir.getRoutes(name());
    }

    /**
     * Build the uri path for a given route
     * @param path The path using any number of subdomains
     * @return The joined URI path
     */
    protected String uri(String... path) {
        if (path.length == 0)
            return "/";
        String uri = String.join("/", path);
        return uri.startsWith("/") ? uri : String.format("/%s", uri);
    }

    /**
     * Get config keys equal to the name of this service, defaults to fully qualified class name
     * If a separate config file exists with the name, it will be used also.
     * @return The service specific config
     */
    @Override
    public final Config conf() {
        String name = name();
        Config config = gungnir.conf();
        return config.getConfig(name)
                .withFallback(ConfigFactory.parseResourcesAnySyntax(name))
                .resolve();
    }

    /**
     * Get the active profiles for the environment
     * @return All the active profiles
     */
    @Override
    public final String[] getProfiles() {
        return gungnir.getProfiles();
    }

    /**
     * lookup user by some token
     * @param token Some user issued token
     * @return A user if token is found, empty otherwise
     */
    @Override
    public final Optional<User> lookupToken(String token) {
        return gungnir.lookupToken(token);
    }
}
