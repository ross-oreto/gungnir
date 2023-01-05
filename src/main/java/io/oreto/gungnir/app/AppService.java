package io.oreto.gungnir.app;

import com.typesafe.config.Config;
import io.javalin.http.Handler;
import io.oreto.gungnir.route.Router;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.oreto.gungnir.route.GungnirApiBuilder.*;

public class AppService extends Service {
    public static String getJavaVersion() {
        return Runtime.version().version()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining("."));
    }
    public static final String JAVA_VER_PROP = "javaVersion";
    public static final String javaVersion = getJavaVersion();

    /**
     * Define this service by providing routing rules, handlers, and authorization
     * @param router The server to provide routes for
     */
    @SuppressWarnings("resource")
    @Override
    public void routing(Router router) {
        Config config = conf();
        router.routes(() -> path(uri(config.getString("path")), () -> {
            post(uri("stop"), stop());
            post(uri("restart"), restart());
            path(uri(config.getString("info.path")), () -> {
                get(info());
                get(uri("env"), env());
                get(uri("routes"), routes());
                get(uri("routes", "{service}"), serviceRoutes());
                get(uri("user"), user());
            });
        }));
    }

    protected Handler stop() {
        return ctx -> {
            CompletableFuture.runAsync(() -> {
                try { Thread.sleep(500); } catch (InterruptedException e) { log.error("", e); }
                gungnir.stop();
            });
            ctx.result("issued server stop");
        };
    }

    protected Handler restart() {
        return ctx -> {
            CompletableFuture.runAsync(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException e) { log.error("", e); }
                gungnir.restart();
            });
            ctx.result("issued server restart");
        };
    }

    protected Handler info() {
        return ctx -> {
            Map<String, Object> info = envObject();
            info.put("routes", gungnir.getRoutes());
            info.put("user", getUser(ctx));
            ctx.json(info);
        };
    }

    protected Map<String, Object> envObject() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put(JAVA_VER_PROP, javaVersion);
        info.put("profiles", getProfiles());
        return info;
    }

    protected Handler env() {
        return ctx -> ctx.json(envObject());
    }

    protected Handler routes() {
        return ctx -> ctx.json(gungnir.getRoutes());
    }

    protected Handler serviceRoutes() {
        return ctx -> ctx.json(gungnir.getRoutes(ctx.pathParam("service")));
    }

    protected Handler user() {
        return ctx -> ctx.json(getUser(ctx));
    }
}
