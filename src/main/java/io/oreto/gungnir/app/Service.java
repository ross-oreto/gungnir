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

import java.util.List;
import java.util.Optional;


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

    public String name() {
        return name;
    }

    /**
     * Define this service by providing routing rules, handlers, and authorization
     * @param router The server to provide routes for
     */
    public abstract void routing(Router router);

    protected List<RouteInfo> getRoutes() {
        return gungnir.getRoutes();
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

    @Override
    public final Config conf() {
        String name = name();
        Config config = gungnir.conf();
        return config.getConfig(name)
                .withFallback(ConfigFactory.parseResourcesAnySyntax(name))
                .resolve();
    }

    @Override
    public final String[] getProfiles() {
        return gungnir.getProfiles();
    }

    @Override
    public final Optional<User> lookupToken(String token) {
        return gungnir.lookupToken(token);
    }
}
