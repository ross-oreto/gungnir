package io.oreto.gungnir.security;

import io.javalin.http.Context;
import io.javalin.security.RouteRole;

import java.util.Objects;
import java.util.Optional;

public interface ContextUser {
    String sessionKey = "subject";
    String attributeKey = "user";

    /**
     * Lookup a user from the context request using:
     *  1. An attribute map if the user has already been found in this context
     *  2. The user session if a session exists
     *  3. Using an authorization token if present
     * @param ctx Provides access to functions for handling the request and response
     * @return A user if present, empty otherwise
     */
    default User getUser(Context ctx) {
        User user = ctx.attribute(attributeKey);
        if (Objects.nonNull(user))
            return user;

        User subject = ctx.sessionAttribute(sessionKey);
        Optional<User> optionalUser;
        if (Objects.nonNull(subject)) {
            optionalUser = Optional.of(subject);
        } else {
            optionalUser = getAuthorizationToken(ctx).flatMap(this::lookupToken);
        }
        optionalUser.ifPresent(u -> {
            if (u.isAuthenticated())
                ctx.attribute(attributeKey, u);
        });
        return optionalUser.orElse(User.UN_AUTHENTICATED);
    }

    /**
     * Is the user authenticated
     * @param ctx Provides access to functions for handling the request and response
     * @return true if the user is authenticated, false otherwise
     */
    default boolean isAuthenticated(Context ctx) {
        return getUser(ctx).isAuthenticated();
    }

    /**
     * check user roles
     * @param ctx Provides access to functions for handling the request and response
     * @return true if user has all the specified roles, false otherwise
     */
    default boolean hasRoles(Context ctx, RouteRole... routeRoles) {
        return getUser(ctx).hasRoles(routeRoles);
    }

    /**
     * check user roles
     * @param ctx Provides access to functions for handling the request and response
     * @return true if user has any of the specified roles, false otherwise
     */
    default boolean hasAnyRole(Context ctx, RouteRole... routeRoles) {
        return getUser(ctx).hasAnyRole(routeRoles);
    }


    /**
     * lookup user by some token
     * @param token Some user issued token
     * @return A user if token is found, empty otherwise
     */
    default Optional<User> lookupToken(String token) {
        return Optional.empty();
    }

    /**
     * Get a token from the authorization request header
     * @param context Provides access to functions for handling the request and response
     * @return The authorization token
     */
    default Optional<String> getAuthorizationToken(Context context) {
        String authorization = context.header("Authorization");
        if (authorization == null || authorization.isBlank())
            return Optional.empty();
        return Optional.of(authorization.replaceFirst("Bearer ", "").trim());
    }
}
