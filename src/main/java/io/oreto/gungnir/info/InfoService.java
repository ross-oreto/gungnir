package io.oreto.gungnir.info;

import io.javalin.http.Handler;
import io.oreto.gungnir.app.Service;
import io.oreto.gungnir.route.Router;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static io.oreto.gungnir.route.GungnirApiBuilder.path;
import static io.oreto.gungnir.route.GungnirApiBuilder.get;


public class InfoService extends Service {
    public static final String JAVA_VER_PROP = "javaVersion";
    public static final String javaVersion = getJavaVersion();

    public static String getJavaVersion() {
        return Runtime.version().version()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining("."));
    }

    /**
     * Define this service by providing routing rules, handlers, and authorization
     *
     * @param router The server to provide routes for
     */
    @SuppressWarnings("resource")
    @Override
    public void routing(Router router) {
        router.routes(() -> path(uri(conf().getString("path")), () -> {
            get(info());
            get(uri("routes"), routes());
            get(uri("routes", "{subdomain}"), subRoutes());
            get(uri("user"), user());
            get(uri("all"), allInfo());
        }));
    }

    protected Map<String, Object> infoObject() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put(JAVA_VER_PROP, javaVersion);
        info.put("profiles", getProfiles());
        return info;
    }

    protected Handler info() {
        return ctx -> ctx.json(infoObject());
    }

    protected Handler routes() {
        return ctx -> ctx.json(getRoutes());
    }

    protected Handler subRoutes() {
        return ctx -> ctx.json(
                getRoutes().stream()
                .filter(route -> route.getPath().startsWith(String.format("/%s", ctx.pathParam("subdomain"))))
                .collect(Collectors.toList())
        );
    }

    protected Handler user() {
        return ctx -> ctx.json(getUser(ctx));
    }

    protected Handler allInfo() {
        return ctx -> {
            Map<String, Object> info = infoObject();
            info.put("routes", getRoutes());
            info.put("user", getUser(ctx));
            ctx.json(info);
        };
    }
}
