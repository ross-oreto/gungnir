package io.oreto.gungnir.app;

import io.javalin.http.Handler;
import io.oreto.gungnir.route.Router;

import java.util.concurrent.CompletableFuture;

import static io.oreto.gungnir.route.GungnirApiBuilder.*;

public class AppService extends Service {
    /**
     * Define this service by providing routing rules, handlers, and authorization
     * @param router The server to provide routes for
     */
    @SuppressWarnings("resource")
    @Override
    public void routing(Router router) {
        router.routes(() -> path(uri(conf().getString("path")), () -> {
            post(uri("stop"), stop());
            post(uri("restart"), restart());
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
}
