package io.oreto.gungnir.error;

import io.javalin.http.ExceptionHandler;
import io.javalin.http.Handler;

import java.util.LinkedHashMap;
import java.util.Map;

public class ErrorHandling {

    private final Map<Class<Exception>, ExceptionHandler<Exception>> exceptionHandlers;

    private final Map<Integer, Handler> errorHandlers;

    public ErrorHandling() {
        this.exceptionHandlers = new LinkedHashMap<>();
        this.errorHandlers = new LinkedHashMap<>();
    }

    public ErrorHandling add(Class<Exception> exception, ExceptionHandler<Exception> handler) {
        exceptionHandlers.put(exception, handler);
        return this;
    }

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
