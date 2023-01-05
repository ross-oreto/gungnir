package io.oreto.gungnir.route;

import io.oreto.gungnir.app.Gungnir;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Handler;
import io.javalin.http.sse.SseClient;
import io.javalin.websocket.WsConfig;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.function.Consumer;

import io.oreto.gungnir.security.Roles;
import org.jetbrains.annotations.NotNull;

/**
 * Static methods for route declarations in Gungnir
 */
@SuppressWarnings("resource")
public class GungnirApiBuilder {

    private static final ThreadLocal<Gungnir> staticGungnir = new ThreadLocal<>();
    private static final ThreadLocal<Deque<String>> pathDeque = ThreadLocal.withInitial(ArrayDeque::new);

    public static void setStaticGungnir(@NotNull Gungnir gungnir) {
        staticGungnir.set(gungnir);
    }

    public static void clearStaticGungnir() {
        staticGungnir.remove();
    }

    /**
     * Prefixes all handlers defined in its scope with the specified path.
     * All paths are normalized, so you can call both
     * path("/path") or path("path") depending on your preference
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void path(@NotNull String path, @NotNull EndpointGroup endpointGroup) {
        path = path.startsWith("/") ? path : "/" + path;
        pathDeque.get().addLast(path);
        endpointGroup.addEndpoints();
        pathDeque.get().removeLast();
    }

    public static String prefixPath(@NotNull String path) {
        if (!path.equals("*")) {
            path = (path.startsWith("/") || path.isEmpty()) ? path : "/" + path;
        }
        return String.join("", pathDeque.get()) + path;
    }

    public static Gungnir staticInstance() {
        Gungnir gungnir = staticGungnir.get();
        if (gungnir == null) {
            throw new IllegalStateException("The static API can only be used within a routes() call.");
        }
        return gungnir;
    }

    // ********************************************************************************************
    // HTTP verbs
    // ********************************************************************************************

    /**
     * Adds a GET request handler for the specified path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void get(@NotNull String path, @NotNull Handler... handler) {
        staticInstance().get(prefixPath(path), handler);
    }

    /**
     * Adds a GET request handler with the given roles for the specified path to the instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void get(@NotNull String path, @NotNull Handler handler, Roles roles) {
        staticInstance().get(prefixPath(path), handler, roles);
    }

    /**
     * Adds a GET request handler for the current path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void get(@NotNull Handler... handler) {
        staticInstance().get(prefixPath(""), handler);
    }

    /**
     * Adds a GET request handler with the given roles for the current path to the instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void get(@NotNull Handler handler, Roles roles) {
        staticInstance().get(prefixPath(""), handler, roles);
    }

    /**
     * Adds a POST request handler for the specified path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void post(@NotNull String path, @NotNull Handler... handler) {
        staticInstance().post(prefixPath(path), handler);
    }

    /**
     * Adds a POST request handler with the given roles for the specified path to the instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void post(@NotNull String path, @NotNull Handler handler, @NotNull Roles roles) {
        staticInstance().post(prefixPath(path), handler, roles);
    }

    /**
     * Adds a POST request handler for the current path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void post(@NotNull Handler... handler) {
        staticInstance().post(prefixPath(""), handler);
    }

    /**
     * Adds a POST request handler with the given roles for the current path to the instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void post(@NotNull Handler handler, @NotNull Roles roles) {
        staticInstance().post(prefixPath(""), handler, roles);
    }

    /**
     * Adds a PUT request handler for the specified path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void put(@NotNull String path, @NotNull Handler... handler) {
        staticInstance().put(prefixPath(path), handler);
    }

    /**
     * Adds a PUT request handler with the given roles for the specified path to the instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void put(@NotNull String path, @NotNull Handler handler, @NotNull Roles roles) {
        staticInstance().put(prefixPath(path), handler, roles);
    }

    /**
     * Adds a PUT request handler for the current path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void put(@NotNull Handler... handler) {
        staticInstance().put(prefixPath(""), handler);
    }

    /**
     * Adds a PUT request handler with the given roles for the current path to the instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void put(@NotNull Handler handler, @NotNull Roles roles) {
        staticInstance().put(prefixPath(""), handler, roles);
    }

    /**
     * Adds a PATCH request handler for the specified path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void patch(@NotNull String path, @NotNull Handler... handler) {
        staticInstance().patch(prefixPath(path), handler);
    }

    /**
     * Adds a PATCH request handler with the given roles for the specified path to the instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void patch(@NotNull String path, @NotNull Handler handler, @NotNull Roles roles) {
        staticInstance().patch(prefixPath(path), handler, roles);
    }

    /**
     * Adds a PATCH request handler for the current path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void patch(@NotNull Handler... handler) {
        staticInstance().patch(prefixPath(""), handler);
    }

    /**
     * Adds a PATCH request handler with the given roles for the current path to the instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void patch(@NotNull Handler handler, @NotNull Roles roles) {
        staticInstance().patch(prefixPath(""), handler, roles);
    }

    /**
     * Adds a DELETE request handler for the specified path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void delete(@NotNull String path, @NotNull Handler... handler) {
        staticInstance().delete(prefixPath(path), handler);
    }

    /**
     * Adds a DELETE request handler with the given roles for the specified path to the instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void delete(@NotNull String path, @NotNull Handler handler, @NotNull Roles roles) {
        staticInstance().delete(prefixPath(path), handler, roles);
    }

    /**
     * Adds a DELETE request handler for the current path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void delete(@NotNull Handler... handler) {
        staticInstance().delete(prefixPath(""), handler);
    }

    /**
     * Adds a DELETE request handler with the given roles for the current path to the instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void delete(@NotNull Handler handler, @NotNull Roles roles) {
        staticInstance().delete(prefixPath(""), handler, roles);
    }

    /**
     * Adds a HEAD request handler for the specified path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void head(@NotNull String path, @NotNull Handler... handler) {
        staticInstance().head(prefixPath(path), handler);
    }

    /**
     * Adds a HEAD request handler with the given roles for the specified path to the instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void head(@NotNull String path, @NotNull Handler handler, @NotNull Roles roles) {
        staticInstance().head(prefixPath(path), handler, roles);
    }

    /**
     * Adds a HEAD request handler for the current path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void head(@NotNull Handler... handler) {
        staticInstance().head(prefixPath(""), handler);
    }

    /**
     * Adds a HEAD request handler with the given roles for the current path to the instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void head(@NotNull Handler handler, @NotNull Roles roles) {
        staticInstance().head(prefixPath(""), handler, roles);
    }

    // ********************************************************************************************
    // Before/after handlers (filters)
    // ********************************************************************************************

    /**
     * Adds a BEFORE request handler for the specified path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void before(@NotNull String path, @NotNull Handler handler) {
        staticInstance().before(prefixPath(path), handler);
    }

    /**
     * Adds a BEFORE request handler for the current path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void before(@NotNull Handler handler) {
        staticInstance().before(prefixPath("*"), handler);
    }

    /**
     * Adds an AFTER request handler for the specified path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void after(@NotNull String path, @NotNull Handler handler) {
        staticInstance().after(prefixPath(path), handler);
    }

    /**
     * Adds a AFTER request handler for the current path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void after(@NotNull Handler handler) {
        staticInstance().after(prefixPath("*"), handler);
    }

    // ********************************************************************************************
    // WebSocket
    // ********************************************************************************************

    /**
     * Adds a WebSocket handler on the specified path.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void ws(@NotNull String path, @NotNull Consumer<WsConfig> ws) {
        staticInstance().ws(prefixPath(path), ws);
    }

    /**
     * Adds a WebSocket handler with the given roles for the specified path.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void ws(@NotNull String path, @NotNull Consumer<WsConfig> ws, @NotNull Roles roles) {
        staticInstance().ws(prefixPath(path), ws, roles);
    }

    /**
     * Adds a WebSocket handler on the current path.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void ws(@NotNull Consumer<WsConfig> ws) {
        staticInstance().ws(prefixPath(""), ws);
    }

    /**
     * Adds a WebSocket handler with the given roles for the current path.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void ws(@NotNull Consumer<WsConfig> ws, @NotNull Roles roles) {
        staticInstance().ws(prefixPath(""), ws, roles);
    }

    /**
     * Adds a WebSocket before handler for the specified path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public Gungnir wsBefore(@NotNull String path, @NotNull Consumer<WsConfig> wsConfig) {
        return staticInstance().wsBefore(prefixPath(path), wsConfig);
    }

    /**
     * Adds a WebSocket before handler for the current path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public Gungnir wsBefore(@NotNull Consumer<WsConfig> wsConfig) {
        return staticInstance().wsBefore(prefixPath("*"), wsConfig);
    }

    /**
     * Adds a WebSocket after handler for the specified path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public Gungnir wsAfter(@NotNull String path, @NotNull Consumer<WsConfig> wsConfig) {
        return staticInstance().wsAfter(prefixPath(path), wsConfig);
    }

    /**
     * Adds a WebSocket after handler for the current path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public Gungnir wsAfter(@NotNull Consumer<WsConfig> wsConfig) {
        return staticInstance().wsAfter(prefixPath("*"), wsConfig);
    }

    // ********************************************************************************************
    // Server-sent events
    // ********************************************************************************************

    public static void sse(@NotNull String path, @NotNull Consumer<SseClient> client) {
        staticInstance().sse(prefixPath(path), client);
    }

    public static void sse(@NotNull String path, @NotNull Consumer<SseClient> client, @NotNull Roles roles) {
        staticInstance().sse(prefixPath(path), client, roles.array());
    }

    public static void sse(@NotNull Consumer<SseClient> client) {
        staticInstance().sse(prefixPath(""), client);
    }

    public static void sse(@NotNull Consumer<SseClient> client, @NotNull Roles roles) {
        staticInstance().sse(prefixPath(""), client, roles);
    }

    // ********************************************************************************************
    // CrudHandler
    // ********************************************************************************************

    /**
     * Adds a CrudHandler handler to the current path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void crud(@NotNull CrudHandler crudHandler) {
        crud("", crudHandler, Roles.empty());
    }

    /**
     * Adds a CrudHandler handler to the current path with the given roles to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     */
    public static void crud(@NotNull CrudHandler crudHandler, @NotNull Roles roles) {
        crud("", crudHandler, roles);
    }

    /**
     * Adds a CrudHandler handler to the specified path to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     *
     * @see CrudHandler
     */
    public static void crud(@NotNull String path, @NotNull CrudHandler crudHandler) {
        crud(path, crudHandler, Roles.empty());
    }

    /**
     * Adds a CrudHandler handler to the specified path with the given roles to the {@link Gungnir} instance.
     * The method can only be called inside a {@link Gungnir#routes(EndpointGroup)}.
     *
     * @see CrudHandler
     */
    public static void crud(@NotNull String path, @NotNull CrudHandler crudHandler, @NotNull Roles roles) {
        String fullPath = prefixPath(path);
        String[] subPaths = Arrays.stream(fullPath.split("/")).filter(it -> !it.isEmpty()).toArray(String[]::new);
        if (subPaths.length < 2) {
            throw new IllegalArgumentException("CrudHandler requires a path like '/resource/{resource-id}'");
        }
        String resourceId = subPaths[subPaths.length - 1];
        if (!(resourceId.startsWith("{") && resourceId.endsWith("}"))) {
            throw new IllegalArgumentException("CrudHandler requires a path-parameter at the end of the provided path, e.g. '/users/{user-id}'");
        }
        String resourceBase = subPaths[subPaths.length - 2];
        if (resourceBase.startsWith("{") || resourceBase.startsWith("<") || resourceBase.endsWith("}") || resourceBase.endsWith(">")) {
            throw new IllegalArgumentException("CrudHandler requires a resource base at the beginning of the provided path, e.g. '/users/{user-id}'");
        }
        staticInstance().get(fullPath, ctx -> crudHandler.getOne(ctx, ctx.pathParam(resourceId)), roles);
        staticInstance().get(fullPath.replace(resourceId, ""), crudHandler::getAll, roles);
        staticInstance().post(fullPath.replace(resourceId, ""), crudHandler::create, roles);
        staticInstance().patch(fullPath, ctx -> crudHandler.update(ctx, ctx.pathParam(resourceId)), roles);
        staticInstance().delete(fullPath, ctx -> crudHandler.delete(ctx, ctx.pathParam(resourceId)), roles);
    }
}

