package io.oreto.gungnir.route;

import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import io.javalin.http.sse.SseClient;
import io.javalin.http.sse.SseHandler;
import io.javalin.security.AccessManager;
import io.javalin.security.RouteRole;
import io.javalin.websocket.WsConfig;
import io.oreto.gungnir.app.Gungnir;
import io.oreto.gungnir.security.Roles;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Router {
    /**
     * Adds request handlers for the specified handlerType and path to the instance.
     * This is the method that all the verb-methods (get/post/put/etc) call.
     *
     * @param httpMethod The http method
     * @param path The path to add the handler on
     * @param handlers The handlers to add
     */
    Gungnir addHandler(@NotNull HandlerType httpMethod, @NotNull String path, @NotNull Handler... handlers);

    /**
     * Adds a request handler for the specified handlerType and path to the instance.
     * Requires an access manager to be set on the instance.
     * This is the method that all the verb-methods (get/post/put/etc) call.
     *
     * @param httpMethod The http method
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    Gungnir addHandler(@NotNull HandlerType httpMethod, @NotNull String path, @NotNull Handler handler, Roles roles);

    /**
     * Adds GET request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    Gungnir get(@NotNull String path, @NotNull Handler... handlers);

    /**
     * Adds a GET request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     * @return this gungnir instance
     */
    Gungnir get(@NotNull String path, @NotNull Handler handler, Roles roles);

    /**
     * Adds PUT request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    Gungnir put(@NotNull String path, @NotNull Handler... handlers);

    /**
     * Adds a PUT request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     * @return this gungnir instance
     */
    Gungnir put(@NotNull String path, @NotNull Handler handler, Roles roles);

    /**
     * Adds POST request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    Gungnir post(@NotNull String path, @NotNull Handler... handlers);

    /**
     * Adds a POST request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     *
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    Gungnir post(@NotNull String path, @NotNull Handler handler, Roles roles);

    /**
     * Adds DELETE request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    Gungnir delete(@NotNull String path, @NotNull Handler... handlers);

    /**
     * Adds a DELETE request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     * @return this gungnir instance
     */
    Gungnir delete(@NotNull String path, @NotNull Handler handler, Roles roles);

    /**
     * Adds HEAD request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    Gungnir head(@NotNull String path, @NotNull Handler... handlers);

    /**
     * Adds a HEAD request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     * @return this gungnir instance
     */
    Gungnir head(@NotNull String path, @NotNull Handler handler, Roles roles);

    /**
     * Adds OPTIONS request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    Gungnir options(@NotNull String path, @NotNull Handler... handlers);

    /**
     * Adds a OPTIONS request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     * @return this gungnir instance
     */
    Gungnir options(@NotNull String path, @NotNull Handler handler, Roles roles);

    /**
     * Adds OPTIONS request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    Gungnir patch(@NotNull String path, @NotNull Handler... handlers);

    /**
     * Adds a PATCH request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    
    Gungnir patch(@NotNull String path, @NotNull Handler handler, Roles roles);

    /**
     * Creates a temporary static instance in the scope of the endpointGroup.
     * Allows you to call get(handler), post(handler), etc. without using the instance prefix.
     * @param endpointGroup The group handler
     */
    Gungnir routes(@NotNull EndpointGroup endpointGroup);

    /**
     * Adds a lambda handler for a Server Sent Event connection on the specified path.
     * @param path The path to add the handler on
     * @param client The handler to add
     */
    
    Gungnir sse(@NotNull String path, @NotNull Consumer<SseClient> client);

    /**
     * Adds a lambda handler for a Server Sent Event connection on the specified path.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    
    Gungnir sse(@NotNull String path, @NotNull SseHandler handler);

    /**
     * Adds a lambda handler for a Server Sent Event connection on the specified path.
     * @param path The path to add the handler on
     * @param before The before handler
     * @param handler The handler to add
     */
    
    Gungnir sse(@NotNull String path, @NotNull Handler before, @NotNull SseHandler handler);

    /**
     * Adds a lambda handler for a Server Sent Event connection on the specified path.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param client The client handler to add
     * @param roles The roles to test for in the access manager
     */
    
    Gungnir sse(@NotNull String path, @NotNull Consumer<SseClient> client, @NotNull Roles roles);

    /**
     * Adds a BEFORE request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    Gungnir before(@NotNull String path, @NotNull Handler handler);

    /**
     * Adds a BEFORE request handler for all routes in the instance.
     * @param handler The handler to add
     */
    Gungnir before(@NotNull Handler handler);

    /**
     * Adds an AFTER request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    Gungnir after(@NotNull String path, @NotNull Handler handler);

    /**
     * Adds an AFTER request handler for all routes in the instance.
     * @param handler The handler to add
     */
    Gungnir after(@NotNull Handler handler);

    /**
     * Adds a WebSocket handler on the specified path.
     * @param path The path to add the handler on
     * @param ws The web socket handler
     */
    Gungnir ws(@NotNull String path, @NotNull Consumer<WsConfig> ws);

    /**
     * Adds a WebSocket handler on the specified path with the specified roles.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param ws The web socket handler
     * @param roles The roles to test for in the access manager
     */
    Gungnir ws(@NotNull String path, @NotNull Consumer<WsConfig> ws, @NotNull Roles roles);

    /**
     * Adds a WebSocket before handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param wsConfig The web socket handler
     */
    Gungnir wsBefore(@NotNull String path, @NotNull Consumer<WsConfig> wsConfig);

    /**
     * Adds a WebSocket before handler for all routes in the instance.
     * @param wsConfig The web socket handler
     */
    Gungnir wsBefore(@NotNull Consumer<WsConfig> wsConfig);

    /**
     * Adds a WebSocket after handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param wsConfig The web socket handler
     */
    Gungnir wsAfter(@NotNull String path, @NotNull Consumer<WsConfig> wsConfig);

    /**
     * Adds a WebSocket after handler for all routes in the instance.
     * @param wsConfig The web socket handler
     */
    Gungnir wsAfter(@NotNull Consumer<WsConfig> wsConfig);

    // -------------------------------- ROUTER PASS THROUGH ------------------------------------------------------------

    /**
     * Adds a request handler for the specified handlerType and path to the instance.
     * Requires an access manager to be set on the instance.
     * This is the method that all the verb-methods (get/post/put/etc) call.
     * @param httpMethod The http method
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     * @see AccessManager
     */
    Gungnir addHandler(@NotNull HandlerType httpMethod, @NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles);

    /**
     * Adds a request handler for the specified handlerType and path to the instance.
     * This is the method that all the verb-methods (get/post/put/etc) call.
     * @param httpMethod The http method
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    Gungnir addHandler(@NotNull HandlerType httpMethod, @NotNull String path, @NotNull Handler handler);

    /**
     * Adds a GET request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    Gungnir get(@NotNull String path, @NotNull Handler handler);

    /**
     * Adds a POST request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    Gungnir post(@NotNull String path, @NotNull Handler handler);

    /**
     * Adds a PUT request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    Gungnir put(@NotNull String path, @NotNull Handler handler);

    /**
     * Adds a PATCH request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    Gungnir patch(@NotNull String path, @NotNull Handler handler);

    /**
     * Adds a DELETE request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    Gungnir delete(@NotNull String path, @NotNull Handler handler);

    /**
     * Adds a HEAD request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    Gungnir head(@NotNull String path, @NotNull Handler handler);

    /**
     * Adds a OPTIONS request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    Gungnir options(@NotNull String path, @NotNull Handler handler);

    /**
     * Adds a GET request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    Gungnir get(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles);

    /**
     * Adds a POST request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    Gungnir post(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles);

    /**
     * Adds a PUT request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    Gungnir put(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles);

    /**
     * Adds a PATCH request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    Gungnir patch(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles);

    /**
     * Adds a DELETE request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    Gungnir delete(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles);

    /**
     * Adds a HEAD request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    Gungnir head(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles);

    /**
     * Adds a OPTIONS request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    Gungnir options(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles);

    /**
     * Adds a lambda handler for a Server Sent Event connection on the specified path.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param client The handler to add
     * @param roles The roles to test for in the access manager
     */
    Gungnir sse(@NotNull String path, @NotNull Consumer<SseClient> client, @NotNull RouteRole... roles);

    /**
     * Adds a WebSocket handler on the specified path with the specified roles.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param ws The handler to add
     * @param roles The roles to test for in the access manager
     */
    Gungnir ws(@NotNull String path, @NotNull Consumer<WsConfig> ws, @NotNull RouteRole... roles);
}
