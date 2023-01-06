package io.oreto.gungnir.app;

import com.typesafe.config.Config;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.sse.SseClient;
import io.javalin.http.sse.SseHandler;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JsonMapper;
import io.javalin.rendering.JavalinRenderer;
import io.javalin.security.AccessManager;
import io.javalin.security.RouteRole;
import io.javalin.websocket.WsConfig;
import io.oreto.gungnir.error.ErrorHandling;
import io.oreto.gungnir.error.ErrorResponse;
import io.oreto.gungnir.render.ViewRenderer;
import io.oreto.gungnir.route.GungnirApiBuilder;
import io.oreto.gungnir.route.RouteInfo;
import io.oreto.gungnir.route.Router;
import io.oreto.gungnir.security.ContextUser;
import io.oreto.gungnir.security.Roles;
import io.oreto.gungnir.security.User;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.session.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.oreto.gungnir.app.Configurable.loadConfig;
import static io.oreto.gungnir.app.IEnvironment.loadProfiles;

/**
 * Extends Javalin to provide extra functionality.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Gungnir extends Javalin implements IEnvironment, Configurable, ContextUser, Router {
    private static final Collection<RouteInfo> NO_ROUTES = List.of();

    public static Gungnir create(Config config) {
        return new Gungnir(config);
    }

    public static Gungnir create() {
        return new Gungnir();
    }

    public static Gungnir create(Consumer<JavalinConfig> config) {
        Gungnir app = new Gungnir();
        JavalinConfig.applyUserConfig(app, app.cfg, config);
        return app;
    }

    /**
     * Defines some very common application environments
     */
    public enum Profile {
        dev, test
    }

    protected final String[] profiles;
    private final Config config;
    protected final Logger log;

    private final Map<String, Collection<RouteInfo>> routeMap;

    protected Gungnir(Config config) {
        this.profiles = loadProfiles();
        this.config = config;
        this.log = LoggerFactory.getLogger(Gungnir.class);
        this.routeMap = new LinkedHashMap<>();

        // log config files loaded
        for (String origin : Arrays.stream(config.origin().description()
                .replace("merge of", "").split(",")).map(String::trim).toList()) {
            log.info("config loaded: {}", origin);
        }
        JavalinConfig.applyUserConfig(this, this.cfg, javalinConfig -> {
            // basic routing settings
            Config serverConfig = config.getConfig("server");
            javalinConfig.routing.contextPath = serverConfig.getString("contextPath");
            javalinConfig.routing.ignoreTrailingSlashes = serverConfig.getBoolean("ignoreTrailingSlashes");
            javalinConfig.routing.treatMultipleSlashesAsSingleSlash = serverConfig.getBoolean("ignoreMultipleSlashes");

            // define events
            this.events(event -> {
                event.serverStarting(() -> { log.info("starting server"); onStarting(); });
                event.serverStarted(() -> { log.info("started server"); onStarted(); });
                event.serverStopping(() -> { log.info("stopping server"); onStopping(); });
                event.serverStopped(() -> { log.info("server stopped"); onStopped(); });
                // store routes
                event.handlerAdded(info -> {
                    RouteInfo routeInfo = RouteInfo.of(info);
                    routeMap.computeIfAbsent(routeInfo.getGroup(), s -> new ArrayList<>()).add(routeInfo);
                });
                event.wsHandlerAdded(info -> {
                    RouteInfo routeInfo = RouteInfo.of(info);
                    routeMap.computeIfAbsent(routeInfo.getGroup(), s -> new ArrayList<>()).add(routeInfo);
                });
            });
            configureGungnir(javalinConfig);

            // any other user defined config
            this.onConfigure().accept(javalinConfig);

            // register services
            registerServices(new ServiceRegistrar(this));
        });
    }

    protected Gungnir() {
        this(loadConfig(loadProfiles()));
    }

    /**
     * Configure features of gungnir
     * @param javalinConfig configuration object for Javalin
     */
    protected void configureGungnir(JavalinConfig javalinConfig) {
        // setup json
        JsonMapper jsonMapper = jsonMapper();
        if (Objects.nonNull(jsonMapper))
            javalinConfig.jsonMapper(jsonMapper);

        // setup session handler
        SessionHandler sessionHandler = sessionHandler();
        if (Objects.nonNull(sessionHandler))
            javalinConfig.jetty.sessionHandler(() -> sessionHandler);

        // setup access manager
        AccessManager accessManager = accessManager();
        if (Objects.nonNull(accessManager))
            javalinConfig.accessManager(accessManager);

        // define error handlers
        ErrorHandling errorHandling = new ErrorHandling();
        errorHandling
                .add(Exception.class, (e, ctx) -> {
                    ErrorResponse errorResponse = ErrorResponse.of(e);
                    log.error("", e);
                    ctx.status(errorResponse.getStatus()).json(errorResponse);
                })
                .add(HttpStatus.NOT_FOUND.getCode(), ctx -> {
                    log.error("{}: {}", ctx.status(), ctx.path());
                    ctx.status(HttpStatus.NOT_FOUND.getCode())
                            .json(ErrorResponse.of(new NotFoundResponse()).withDetail("path", ctx.path()));
                });
        errorHandling(errorHandling);
        errorHandling.getExceptionHandlers().forEach(this::exception);
        errorHandling.getErrorHandlers().forEach(this::error);

        // register view/template renderer
        ViewRenderer viewRenderer = viewRenderer();
        if (Objects.nonNull(viewRenderer))
            JavalinRenderer.register(viewRenderer.getRenderer(), viewRenderer.extensions());

        configureStaticFiles(javalinConfig);
        configureSpa(javalinConfig);
        configureCors(javalinConfig);
    }

    /**
     * Define configuration for static file handler
     * @param javalinConfig configuration object for Javalin
     */
    protected void configureStaticFiles(JavalinConfig javalinConfig) {
        Config staticFilesConfig = config.getConfig("staticFiles");
        if (staticFilesConfig.getBoolean("enabled")) {
            if (staticFilesConfig.getBoolean("enableWebjars")) {
                javalinConfig.staticFiles.enableWebjars();
            }
            javalinConfig.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = staticFilesConfig.getString("hosted");
                staticFiles.directory = staticFilesConfig.getString("directory");
                staticFiles.location = staticFilesConfig.getString("location") .equals("classpath")
                        ? Location.CLASSPATH
                        : Location.EXTERNAL;
                staticFiles.precompress = staticFilesConfig.getBoolean("precompress");
                Config headers = staticFilesConfig.getConfig("headers");
                staticFiles.headers = headers.root().keySet().stream()
                        .collect(Collectors.toMap(it -> it, headers::getString));
                Set<String> skipFiles = new HashSet<>(staticFilesConfig.getStringList("skipFiles"));
                if (!skipFiles.isEmpty()) {
                    staticFiles.skipFileFunction = req -> {
                        String uri = req.getRequestURI();
                        if (uri.contains("/")) {
                            return uri.length() != 1 && skipFiles.contains(uri.substring(uri.lastIndexOf('/')));
                        } else {
                            return skipFiles.contains(uri);
                        }
                    };
                }
            });
        }
    }

    /**
     * Define configuration for spa root config
     * @param javalinConfig configuration object for Javalin
     */
    protected void configureSpa(JavalinConfig javalinConfig) {
        Config spaConfig = config.getConfig("spa");
        Map<String, String> spaMap = spaConfig.root().keySet().stream()
                .collect(Collectors.toMap(it -> it, spaConfig::getString));
        spaMap.forEach(javalinConfig.spaRoot::addFile);
    }

    /**
     * Define configuration for CORS handler
     * @param javalinConfig configuration object for Javalin
     */
    protected void configureCors(JavalinConfig javalinConfig) {
        Config corsConf = config.getConfig("cors");
        if (corsConf.getBoolean("enabled")) {
            List<String> hosts = corsConf.getStringList("hosts");
            javalinConfig.plugins.enableCors(cors -> {
                cors.add(it -> {
                    if (hosts.isEmpty())
                        it.anyHost();
                    else if (hosts.size() == 1)
                        it.allowHost(hosts.get(0));
                    else {
                        it.allowHost(hosts.get(0), hosts.subList(1, hosts.size()).toArray(String[]::new));
                    }
                    it.allowCredentials = corsConf.getBoolean("allowCredentials");
                    for (String header : corsConf.getStringList("exposedHeaders"))
                        it.exposeHeader(header);
                });
            });
        }
    }

    /**
     * Start gungnir on the specified host and port
     * @param host The host IP to bind to
     * @param port to run on
     * @return this gungnir instance
     */
    @Override
    public Gungnir start(String host, int port) {
        super.start(host, port);
        return this;
    }

    /**
     * Start gungnir on the specified port
     * @param port to run on
     * @return this gungnir instance
     */
    @Override
    public Gungnir start(int port) {
        return start(config.getString("server.host"), port);
    }

    /**
     * Start gungnir and register start/stop events
     */
    public Gungnir start() {
        jettyServer.setServerHost(config.getString("server.host"));
        jettyServer.setServerPort(config.getInt("server.port"));
        super.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        return this;
    }

    /**
     * Synchronously stops the application instance.
     * <p>
     * Recommended to use {@link Javalin#close} instead with Java's try-with-resources
     * or Kotlin's {@code use}. This differs from {@link Javalin#close} by
     * firing lifecycle events even if the server is stopping or already stopped.
     * This could cause your listeners to observe nonsensical state transitions.
     * E.g. started -> stopping -> stopped -> stopping -> stopped.
     *
     * @return stopped application instance.
     * @see Javalin#close()
     */
    public Gungnir stop() {
        super.stop();
        return this;
    }

    /**
     * Stop and start the server
     * @return The new started gungnir server
     */
    @SuppressWarnings("resource")
    public Gungnir restart() {
        Gungnir gungnir = this.stop();
        return new Gungnir(conf()) {
            @Override
            protected JsonMapper jsonMapper() {
                return gungnir.jsonMapper();
            }
            @Override
            protected AccessManager accessManager() {
                return gungnir.accessManager();
            }
            @Override
            protected void errorHandling(ErrorHandling errorHandling) {
                gungnir.errorHandling(errorHandling);
            }
            @Override
            protected SessionHandler sessionHandler() {
                return gungnir.sessionHandler();
            }
            @Override
            protected AbstractSessionCache sessionCache(SessionHandler sessionHandler) {
                return gungnir.sessionCache(sessionHandler);
            }
            @Override
            protected AbstractSessionDataStore sessionDataStore() {
                return gungnir.sessionDataStore();
            }
            @Override
            protected ViewRenderer viewRenderer() {
                return gungnir.viewRenderer();
            }
            @Override
            protected void registerServices(ServiceRegistrar registrar) {
                gungnir.registerServices(registrar);
            }
            @Override
            protected void configureStaticFiles(JavalinConfig javalinConfig) {
                gungnir.configureCors(javalinConfig);
            }
            @Override
            protected void configureCors(JavalinConfig javalinConfig) {
                gungnir.configureCors(javalinConfig);
            }
        }.start();
    }

    /**
     * Get the active profiles for the environment
     * @return All the active profiles
     */
    @Override
    public String[] getProfiles() {
        return profiles;
    }

    /**
     * Provide the configuration properties
     * @return The config
     */
    @Override
    public Config conf() {
        return config;
    }

    /**
     * Get the application routes
     * @return List of app routes
     */
    public Collection<RouteInfo> getRoutes() {
        return routeMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * Get the application routes for a particular group
     * @return List of grouped app routes
     */
    public Collection<RouteInfo> getRoutes(String group) {
        return routeMap.getOrDefault(group, NO_ROUTES);
    }

    /**
     * Additional configuration to the server
     * @return The configuration consumer
     */
    public Consumer<JavalinConfig> onConfigure() {
        return javalin -> {};
    }

    /**
     * Register each service instance with the server
     * @param services The services to register
     * @return this gungnir instance
     */
    public Gungnir register(Service... services) {
        for (Service service : services)
            service.setGungnir(this);
        return this;
    }

    /**
     * Override this to add more custom exception/error handlers
     * @param errorHandling Object to define error handlers
     */
    protected void errorHandling(ErrorHandling errorHandling) {
    }

    protected JsonMapper jsonMapper() {
        return null;
    }

    /**
     * Supply the session handler
     * @return The session handler
     */
    protected SessionHandler sessionHandler() {
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setSessionCache(sessionCache(sessionHandler));
        sessionHandler.setSameSite(HttpCookie.SameSite.STRICT);
        return sessionHandler;
    }

    /**
     * Define the cache used by the session handler
     * @return The session cache
     */
    protected AbstractSessionCache sessionCache(SessionHandler sessionHandler) {
        AbstractSessionCache sessionCache = new DefaultSessionCache(sessionHandler);
        sessionCache.setSessionDataStore(sessionDataStore());
        return sessionCache;
    }

    /**
     * Define the session data store
     * @return The session data store
     */
    protected AbstractSessionDataStore sessionDataStore() {
        FileSessionDataStore fileSessionDataStore = new FileSessionDataStore();
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File storeDir = new File(baseDir, "javalin-session-store");
        if (storeDir.mkdir()) {
            log.info("created directory {}", storeDir);
        }
        fileSessionDataStore.setStoreDir(storeDir);
        return fileSessionDataStore;
    }

    /**
     * Define the accessManager which lets you set per-endpoint authentication and/or authorization
     * @return An access manager
     */
    protected AccessManager accessManager() {
        return (handler, ctx, routeRoles) -> {
            if (routeRoles.isEmpty())
                handler.handle(ctx);
            else {
                // equivalent to contains any role
                User user = getUser(ctx);
                boolean match = false;
                for (RouteRole role : user.getRoles()) {
                    if (routeRoles.contains(role)) {
                        match = true;
                        break;
                    }
                }
                if (match)
                    handler.handle(ctx);
                else
                    ctx.status(HttpStatus.FORBIDDEN);
            }
        };
    }

    protected ViewRenderer viewRenderer() {
        return null;
    }

    protected void registerServices(ServiceRegistrar registrar) {
    }

    /**
     * Override to perform tasks when server is starting
     */
    public void onStarting() {}

    /**
     * Override to perform tasks when server is started
     */
    public void onStarted() {}

    /**
     * Override to perform tasks when server is stopping
     */
    public void onStopping() {}

    /**
     * Override to perform tasks when server is stopped
     */
    public void onStopped() {}

    // ------- ROUTER implementation -----------------------------------------------------------------------------------
    /**
     * If there are multiple handlers (n > 1), then use before handler
     * @param path to add before handlers on
     * @param handlers The handlers to add
     */
    private void beforeHandlers(@NotNull String path, @NotNull Handler... handlers) {
        int last = handlers.length - 1;
        for (int i = 0; i < last; i++) {
            this.before(path, handlers[i]);
        }
    }

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
    @Override
    public Gungnir addHandler(@NotNull HandlerType httpMethod
            , @NotNull String path
            , @NotNull Handler handler
            , @NotNull Roles roles) {
        super.addHandler(httpMethod, path, handler, roles.array());
        return this;
    }

    /**
     * Adds request handlers for the specified handlerType and path to the instance.
     * This is the method that all the verb-methods (get/post/put/etc) call.
     *
     * @param httpMethod The http method
     * @param path       The path to add the handler on
     * @param handlers   The handlers to add
     */
    @Override
    public Gungnir addHandler(@NotNull HandlerType httpMethod, @NotNull String path, @NotNull Handler... handlers) {
        beforeHandlers(path, handlers);
        this.addHandler(httpMethod, path, handlers[handlers.length - 1]);
        return this;
    }

    /**
     * Adds GET request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    public Gungnir get(@NotNull String path, @NotNull Handler... handlers) {
        beforeHandlers(path, handlers);
        this.get(path, handlers[handlers.length - 1]);
        return this;
    }

    /**
     * Adds a GET request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     * @return this gungnir instance
     */
    @Override
    public Gungnir get(@NotNull String path, @NotNull Handler handler, Roles roles) {
        super.get(path, handler, roles.array());
        return this;
    }

    /**
     * Adds PUT request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    public Gungnir put(@NotNull String path, @NotNull Handler... handlers) {
        beforeHandlers(path, handlers);
        this.put(path, handlers[handlers.length - 1]);
        return this;
    }

    /**
     * Adds a PUT request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     * @return this gungnir instance
     */
    @Override
    public Gungnir put(@NotNull String path, @NotNull Handler handler, Roles roles) {
        super.put(path, handler, roles.array());
        return this;
    }

    /**
     * Adds POST request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    public Gungnir post(@NotNull String path, @NotNull Handler... handlers) {
        beforeHandlers(path, handlers);
        this.post(path, handlers[handlers.length - 1]);
        return this;
    }

    /**
     * Adds a POST request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir post(@NotNull String path, @NotNull Handler handler, Roles roles) {
        super.post(path, handler, roles.array());
        return this;
    }

    /**
     * Adds DELETE request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    public Gungnir delete(@NotNull String path, @NotNull Handler... handlers) {
        beforeHandlers(path, handlers);
        this.delete(path, handlers[handlers.length - 1]);
        return this;
    }

    /**
     * Adds a DELETE request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir delete(@NotNull String path, @NotNull Handler handler, Roles roles) {
        super.delete(path, handler, roles.array());
        return this;
    }

    /**
     * Adds HEAD request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    public Gungnir head(@NotNull String path, @NotNull Handler... handlers) {
        beforeHandlers(path, handlers);
        this.head(path, handlers[handlers.length - 1]);
        return this;
    }

    /**
     * Adds a HEAD request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir head(@NotNull String path, @NotNull Handler handler, Roles roles) {
        super.head(path, handler, roles.array());
        return this;
    }

    /**
     * Adds OPTIONS request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    public Gungnir options(@NotNull String path, @NotNull Handler... handlers) {
        beforeHandlers(path, handlers);
        this.options(path, handlers[handlers.length - 1]);
        return this;
    }

    /**
     * Adds a OPTIONS request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir options(@NotNull String path, @NotNull Handler handler, Roles roles) {
        super.options(path, handler, roles.array());
        return this;
    }

    /**
     * Adds OPTIONS request handlers for the specified path to the instance.
     * @param path The path to add the handlers on
     * @param handlers The handlers to add
     * @return this gungnir instance
     */
    public Gungnir patch(@NotNull String path, @NotNull Handler... handlers) {
        beforeHandlers(path, handlers);
        this.patch(path, handlers[handlers.length - 1]);
        return this;
    }

    /**
     * Adds a PATCH request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir patch(@NotNull String path, @NotNull Handler handler, Roles roles) {
        super.patch(path, handler, roles.array());
        return this;
    }

    /**
     * Creates a temporary static instance in the scope of the endpointGroup.
     * Allows you to call get(handler), post(handler), etc. without using the instance prefix.
     * @param endpointGroup The group handler
     */
    @Override
    public Gungnir routes(@NotNull EndpointGroup endpointGroup) {
        GungnirApiBuilder.setStaticGungnir(this);
        try {
            endpointGroup.addEndpoints();
        } finally {
            GungnirApiBuilder.clearStaticGungnir();
        }
        return this;
    }

    /**
     * Adds a lambda handler for a Server Sent Event connection on the specified path.
     * @param path The path to add the handler on
     * @param client The handler to add
     */
    @Override
    public Gungnir sse(@NotNull String path, @NotNull Consumer<SseClient> client) {
        super.sse(path, client);
        return this;
    }

    /**
     * Adds a lambda handler for a Server Sent Event connection on the specified path.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    @Override
    public Gungnir sse(@NotNull String path, @NotNull SseHandler handler) {
        super.sse(path, handler);
        return this;
    }

    /**
     * Adds a lambda handler for a Server Sent Event connection on the specified path.
     * @param path The path to add the handler on
     * @param before The before handler
     * @param handler The handler to add
     */
    @Override
    public Gungnir sse(@NotNull String path, @NotNull Handler before, @NotNull SseHandler handler) {
        super.before(before).sse(path, handler);
        return this;
    }

    /**
     * Adds a lambda handler for a Server Sent Event connection on the specified path.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param client The client handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir sse(@NotNull String path, @NotNull Consumer<SseClient> client, @NotNull Roles roles) {
        super.sse(path, client, roles.array());
        return this;
    }

    /**
     * Adds a BEFORE request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    @Override
    public Gungnir before(@NotNull String path, @NotNull Handler handler) {
        super.before(path, handler);
        return this;
    }

    /**
     * Adds a BEFORE request handler for all routes in the instance.
     * @param handler The handler to add
     */
    @Override
    public Gungnir before(@NotNull Handler handler) {
        super.before(handler);
        return this;
    }

    /**
     * Adds an AFTER request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    @Override
    public Gungnir after(@NotNull String path, @NotNull Handler handler) {
        super.after(path, handler);
        return this;
    }

    /**
     * Adds an AFTER request handler for all routes in the instance.
     * @param handler The handler to add
     */
    @Override
    public Gungnir after(@NotNull Handler handler) {
        super.after(handler);
        return this;
    }

    /**
     * Adds a WebSocket handler on the specified path.
     * @param path The path to add the handler on
     * @param ws The web socket handler
     */
    @Override
    public Gungnir ws(@NotNull String path, @NotNull Consumer<WsConfig> ws) {
        super.ws(path, ws);
        return this;
    }

    /**
     * Adds a WebSocket handler on the specified path with the specified roles.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param ws The web socket handler
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir ws(@NotNull String path, @NotNull Consumer<WsConfig> ws, @NotNull Roles roles) {
        super.ws(path, ws, roles.array());
        return this;
    }

    /**
     * Adds a WebSocket before handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param wsConfig The web socket handler
     */
    @Override
    public Gungnir wsBefore(@NotNull String path, @NotNull Consumer<WsConfig> wsConfig) {
        super.wsBefore(path, wsConfig);
        return this;
    }

    /**
     * Adds a WebSocket before handler for all routes in the instance.
     * @param wsConfig The web socket handler
     */
    @Override
    public Gungnir wsBefore(@NotNull Consumer<WsConfig> wsConfig) {
        super.wsBefore(wsConfig);
        return this;
    }

    /**
     * Adds a WebSocket after handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param wsConfig The web socket handler
     */
    @Override
    public Gungnir wsAfter(@NotNull String path, @NotNull Consumer<WsConfig> wsConfig) {
        super.wsAfter(path, wsConfig);
        return this;
    }

    /**
     * Adds a WebSocket after handler for all routes in the instance.
     * @param wsConfig The web socket handler
     */
    @Override
    public Gungnir wsAfter(@NotNull Consumer<WsConfig> wsConfig) {
        super.wsAfter(wsConfig);
        return this;
    }

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
    @Override
    public Gungnir addHandler(@NotNull HandlerType httpMethod, @NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles) {
        super.addHandler(httpMethod, path, handler, roles);
        return this;
    }

    /**
     * Adds a request handler for the specified handlerType and path to the instance.
     * This is the method that all the verb-methods (get/post/put/etc) call.
     * @param httpMethod The http method
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    @Override
    public Gungnir addHandler(@NotNull HandlerType httpMethod, @NotNull String path, @NotNull Handler handler) {
        super.addHandler(httpMethod, path, handler);
        return this;
    }

    /**
     * Adds a GET request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    @Override
    public Gungnir get(@NotNull String path, @NotNull Handler handler) {
        super.get(path, handler);
        return this;
    }

    /**
     * Adds a POST request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    @Override
    public Gungnir post(@NotNull String path, @NotNull Handler handler) {
        super.post(path, handler);
        return this;
    }

    /**
     * Adds a PUT request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    @Override
    public Gungnir put(@NotNull String path, @NotNull Handler handler) {
        super.put(path, handler);
        return this;
    }

    /**
     * Adds a PATCH request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    @Override
    public Gungnir patch(@NotNull String path, @NotNull Handler handler) {
        super.patch(path, handler);
        return this;
    }

    /**
     * Adds a DELETE request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    @Override
    public Gungnir delete(@NotNull String path, @NotNull Handler handler) {
        super.delete(path, handler);
        return this;
    }

    /**
     * Adds a HEAD request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    @Override
    public Gungnir head(@NotNull String path, @NotNull Handler handler) {
        super.head(path, handler);
        return this;
    }

    /**
     * Adds a OPTIONS request handler for the specified path to the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     */
    @Override
    public Gungnir options(@NotNull String path, @NotNull Handler handler) {
        super.options(path, handler);
        return this;
    }

    /**
     * Adds a GET request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir get(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles) {
        super.get(path, handler, roles);
        return this;
    }

    /**
     * Adds a POST request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir post(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles) {
        super.post(path, handler, roles);
        return this;
    }

    /**
     * Adds a PUT request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir put(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles) {
        super.put(path, handler, roles);
        return this;
    }

    /**
     * Adds a PATCH request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir patch(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles) {
        super.patch(path, handler, roles);
        return this;
    }

    /**
     * Adds a DELETE request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir delete(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles) {
        super.delete(path, handler, roles);
        return this;
    }

    /**
     * Adds a HEAD request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir head(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles) {
        super.head(path, handler, roles);
        return this;
    }

    /**
     * Adds a OPTIONS request handler with the given roles for the specified path to the instance.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param handler The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir options(@NotNull String path, @NotNull Handler handler, @NotNull RouteRole... roles) {
        super.options(path, handler, roles);
        return this;
    }

    /**
     * Adds a lambda handler for a Server Sent Event connection on the specified path.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param client The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir sse(@NotNull String path, @NotNull Consumer<SseClient> client, @NotNull RouteRole... roles) {
        super.sse(path, client, roles);
        return this;
    }

    /**
     * Adds a WebSocket handler on the specified path with the specified roles.
     * Requires an access manager to be set on the instance.
     * @param path The path to add the handler on
     * @param ws The handler to add
     * @param roles The roles to test for in the access manager
     */
    @Override
    public Gungnir ws(@NotNull String path, @NotNull Consumer<WsConfig> ws, @NotNull RouteRole... roles) {
        super.ws(path, ws, roles);
        return this;
    }
}
