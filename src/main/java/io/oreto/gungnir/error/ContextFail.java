package io.oreto.gungnir.error;

import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.HttpStatus;
import io.oreto.gungnir.http.ShortCircuit;

/**
 * Provides error responses based on request errors
 */
public interface ContextFail extends ShortCircuit {
    /**
     * Failure response based on the provided error
     * @param ctx Provides access to functions for handling the request and response
     * @param error The error to fail with
     */
    default void fail(Context ctx, HttpResponseException error) {
        ctx.status(error.getStatus()).json(error);
        stopContext(ctx);
    }

    /**
     * Failure response based on the provided status
     * @param ctx Provides access to functions for handling the request and response
     * @param status The http status response code
     * @param message The error message
     */
    default void fail(Context ctx, int status, String message) {
        fail(ctx, new HttpResponseException(status, message));
    }

    /**
     * Failure response based on the provided error
     * @param error The error to throw
     */
    default void fail(HttpResponseException error) {
        throw error;
    }

    /**
     * Failure response based on the provided status
     * @param status The http status response code
     * @param message The error message to throw
     */
    default void fail(int status, String message) {
        throw new HttpResponseException(status, message);
    }

    /**
     * Failure response based on the provided status
     * @param httpStatus The http status response to throw
     */
    default void fail(HttpStatus httpStatus) {
        throw new HttpResponseException(httpStatus.getCode(), httpStatus.getMessage());
    }
}
