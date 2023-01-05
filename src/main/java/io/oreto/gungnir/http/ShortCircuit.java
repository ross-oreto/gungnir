package io.oreto.gungnir.http;

import io.javalin.http.Context;
import io.javalin.http.servlet.JavalinServletContext;

public interface ShortCircuit {
    default void stopContext(Context ctx) {
        ((JavalinServletContext) ctx).getTasks().clear(); // skip any remaining tasks for this request
    }
}
