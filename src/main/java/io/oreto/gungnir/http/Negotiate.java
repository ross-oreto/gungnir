package io.oreto.gungnir.http;


import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.Header;
import io.javalin.http.HttpStatus;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Support content negotiation by check the request Accept header
 */
public class Negotiate {
    protected static final Handler DEFAULT_FALLBACK = ctx -> ctx.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE);

    public static Negotiate create() {
        return new Negotiate();
    }

    /**
     * Check if the request Accept header matches any of the media types
     * @param ctx Provides access to functions for handling the request and response
     * @param mediaTypes The request media type
     * @return True if the accept header matches any of the mediaTypes, false otherwise
     */
    static boolean matches(Context ctx, MediaType... mediaTypes) {
        String header = ctx.header(Header.ACCEPT);
        if (header == null)
            return false;
        Set<MediaType> accepts = Arrays.stream(header.split(","))
                .map(String::trim)
                .map(MediaType::parse)
                .collect(Collectors.toSet());
        for (MediaType mediaType : mediaTypes) {
            if (accepts.contains(mediaType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the request Accept header matches any of the media types
     * @param ctx Provides access to functions for handling the request and response
     * @param mediaTypes The request media type as a string
     * @return True if the accept header matches any of the mediaTypes, false otherwise
     */
    static boolean matches(Context ctx, String... mediaTypes) {
        return matches(ctx, Arrays.stream(mediaTypes).map(String::trim).map(MediaType::parse).toArray(MediaType[]::new));
    }

    private final Map<MediaType, Handler> handlers;
    private Handler fallback;

    protected Negotiate() {
        this.handlers = new LinkedHashMap<>();
    }

    /**
     * Define handler for the specified media type
     * @param mediaType The media type to handle
     * @param handler The handler action for the media type
     * @return this negotiate object
     */
    public Negotiate on(MediaType mediaType, Handler handler) {
        handlers.put(mediaType, handler);
        return this;
    }

    /**
     * Define handler for html
     * @param handler The handler action for html
     * @return this negotiate object
     */
    public Negotiate html(Handler handler) {
        return on(MediaType.TEXT_HTML, handler).on(MediaType.APPLICATION_XHTML_XML, handler);
    }

    /**
     * Define handler for json
     * @param handler The handler action for json
     * @return this negotiate object
     */
    public Negotiate json(Handler handler) {
        return on(MediaType.APPLICATION_JSON, handler);
    }

    /**
     * If html matches render the template with specified model, fallback to json
     * @param file The template file to render
     * @param model The model to pass to the template
     * @return this negotiate object
     */
    public Handler html_fallback_json(String file, Map<String, Object> model) {
        return html(ctx -> ctx.render(file, model)).otherwise(ctx -> ctx.json(model));
    }

    /**
     * Set the Handler to use when the media type does not match the request.
     * @param fallback handler to use when the media type does not match
     * @return The created handler
     */
    public Handler otherwise(Handler fallback) {
        this.fallback = fallback;
        return handle();
    }

    /**
     * Return the handler which tests the Accept header and routes to the matching media type handler
     * @return The created handler
     */
    public Handler handle() {
        return ctx -> {
            for (Map.Entry<MediaType, Handler> entry : handlers.entrySet()) {
                if (matches(ctx, entry.getKey())) {
                    entry.getValue().handle(ctx);
                    return;
                }
            }
            if (fallback == null)
                DEFAULT_FALLBACK.handle(ctx);
            else
                fallback.handle(ctx);
        };
    }
}
