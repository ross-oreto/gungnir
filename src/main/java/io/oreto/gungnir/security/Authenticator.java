package io.oreto.gungnir.security;

import io.javalin.http.Context;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Provide application authentication
 */
public interface Authenticator {
    /**
     * Generate a unique token
     * @return A UUID string token
     */
    static String generateToken() {
        return String.format("%d-%s", Instant.now().toEpochMilli(), UUID.randomUUID());
    }

    /**
     * Authenticate a user from the context request
     * @param context Provides access to functions for handling the request and response
     * @return A user if authentication is successful, empty otherwise
     */
    Optional<User> authenticate(Context context);
}
