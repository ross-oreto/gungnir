package io.oreto.gungnir.security;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.oreto.gungnir.app.Service;
import io.oreto.gungnir.route.Router;

import java.util.Objects;
import java.util.Optional;

public abstract class LoginService extends Service {
    protected final Authenticator authenticator;

    public LoginService(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * Define this service by providing routing rules, handlers, and authorization
     * @param router The app to provide routes for
     */
    @SuppressWarnings("resource")
    @Override
    public void routing(Router router) {
        router.get(uri(conf().getString("login.uri")), login())
                .post(uri(conf().getString("logout.uri")), logout());
    }

    /**
     * Provide login logic
     */
    protected Handler login() {
        return ctx -> {
            Optional<User> user = authenticator.authenticate(ctx);
            if (user.isPresent()) {
                startSession(ctx, user.get());
                handleLoginSuccess().handle(ctx);
            } else {
                handleLoginFailure().handle(ctx);
            }
        };
    }

    /**
     * User logout handler
     */
    protected Handler logout() {
        return ctx -> {
            User user = getUser(ctx);
            if (Objects.nonNull(user) && user.isAuthenticated()) {
                endSession(ctx, user);
                handleLogout().handle(ctx);
            }
        };
    }

    /**
     * Begin a user session
     * @param ctx Provides access to functions for handling the request and response
     * @param user Represents an authenticated user
     */
    protected void startSession(Context ctx, User user) {
        ctx.sessionAttribute(sessionKey, user);
        ctx.attribute(attributeKey, user);
    }

    /**
     * End a user session
     * @param ctx Provides access to functions for handling the request and response
     * @param user Represents an authenticated user
     */
    protected void endSession(Context ctx, User user) {
        ctx.consumeSessionAttribute(sessionKey);
        ctx.attribute(attributeKey, null);
        getAuthorizationToken(ctx).ifPresent(s -> removeToken(user, s));
    }

    /**
     * Override this if your app uses access tokens to remove tokens during logout
     * @param user Represents an authenticated user
     * @param token The user access token
     */
    protected void removeToken(User user, String token) {
    }

    /**
     * Handle successful user login
     * @return The login handler
     */
    protected abstract Handler handleLoginSuccess();

    /**
     * Handle failed user login
     * @return The failed login handler
     */
    protected abstract Handler handleLoginFailure();

    /**
     * Logic to run when a user logs out
     * @return The logout handler
     */
    protected Handler handleLogout() {
        return ctx -> {};
    }
}
