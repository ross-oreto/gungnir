package io.oreto.gungnir.error;

import io.javalin.http.ExceptionHandler;
import io.javalin.http.Handler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides a fluent api object that just registers error handlers for a particular exception type or error code.
 * Useful to encapsulate with a method which only adds error handlers.
 */
public class ErrorHandling {

    private final Map<Class<Exception>, ExceptionHandler<Exception>> exceptionHandlers;

    private final Map<Integer, Handler> errorHandlers;

    public ErrorHandling() {
        this.exceptionHandlers = new LinkedHashMap<>();
        this.errorHandlers = new LinkedHashMap<>();
    }

    /**
     * Add a handler for a specific Exception type
     * @param exception The exception class
     * @param handler The exception handler
     * @return this error handling object
     */
    public ErrorHandling add(Class<Exception> exception, ExceptionHandler<Exception> handler) {
        exceptionHandlers.put(exception, handler);
        return this;
    }

    /**
     * Add a handler for a specific error code
     * @param error The error status code
     * @param handler The exception handler
     * @return this error handling object
     */
    public ErrorHandling add(Integer error, Handler handler) {
        errorHandlers.put(error, handler);
        return this;
    }

    public Map<Class<Exception>, ExceptionHandler<Exception>> getExceptionHandlers() {
        return exceptionHandlers;
    }

    public Map<Integer, Handler> getErrorHandlers() {
        return errorHandlers;
    }
}
