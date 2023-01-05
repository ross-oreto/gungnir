package io.oreto.gungnir.error;

import io.javalin.http.HttpResponseException;
import io.javalin.http.HttpStatus;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Provides a standard http response for errors/exceptions
 */
public class ErrorResponse {
    /**
     * Construct a new error response from a http exception
     * @param exception The http response exception
     * @return A new error response
     */
    public static ErrorResponse of(HttpResponseException exception) {
        return new ErrorResponse(exception);
    }

    /**
     * Construct a new error response from any Throwable
     * @param throwable The Throwable exception
     * @return A new error response
     */
    public static ErrorResponse of(Throwable throwable) {
        return new ErrorResponse(throwable);
    }

    private final int status;
    private final String message;

    private final Map<String, String> details;

    private ErrorResponse(HttpResponseException exception) {
        this.status = exception.getStatus();
        this.message = exception.getMessage();
        this.details = new LinkedHashMap<>();
        this.details.putAll(exception.getDetails());
    }

    private ErrorResponse(Throwable throwable) {
        this.status = HttpStatus.INTERNAL_SERVER_ERROR.getCode();
        String message = throwable.getMessage();
        this.message = message == null ? throwable.getClass().getName() : message;
        this.details = new HashMap<>();
        details.put("type", throwable.getClass().getName());
        if (Objects.nonNull(throwable.getCause())) {
            details.put(
                    "cause"
                    , throwable.getCause() == null ? throwable.getCause().getClass().getName() : throwable.getMessage()
            );
        }
    }

    public ErrorResponse withDetail(String key, String value) {
        details.put(key, value);
        return this;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
