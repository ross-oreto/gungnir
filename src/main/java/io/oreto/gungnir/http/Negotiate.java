package io.oreto.gungnir.http;


import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Negotiate {
    protected static final Handler DEFAULT_FALLBACK = ctx -> ctx.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE);

    public static Negotiate create() {
        return new Negotiate();
    }

    static boolean matches(Context ctx, MediaType... mediaTypes) {
        String header = ctx.header("Accept");
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

    static boolean matches(Context ctx, String... mediaTypes) {
        return matches(ctx, Arrays.stream(mediaTypes).map(String::trim).map(MediaType::parse).toArray(MediaType[]::new));
    }

    private final Map<MediaType, Handler> handlers;
    private Handler fallback;

    protected Negotiate() {
        this.handlers = new LinkedHashMap<>();
    }

    public Negotiate on(MediaType mediaType, Handler handler) {
        handlers.put(mediaType, handler);
        return this;
    }

    public Negotiate html(Handler handler) {
        return on(MediaType.TEXT_HTML, handler).on(MediaType.APPLICATION_XHTML_XML, handler);
    }

    public Negotiate json(Handler handler) {
        return on(MediaType.APPLICATION_JSON, handler);
    }

    public Handler html_fallback_json(String file, Map<String, Object> model) {
        return html(ctx -> ctx.render(file, model)).otherwise(ctx -> ctx.json(model));
    }

    public Handler otherwise(Handler fallback) {
        this.fallback = fallback;
        return handle();
    }

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
