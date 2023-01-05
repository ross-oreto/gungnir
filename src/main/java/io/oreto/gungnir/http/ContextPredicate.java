package io.oreto.gungnir.http;

import io.javalin.http.*;
import io.javalin.security.RouteRole;
import io.oreto.gungnir.error.ContextFail;
import io.oreto.gungnir.security.ContextUser;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Fluent API that allows to create chains of request conditions for composing
 * logical expressions to match requests.
 *
 *<p>
 * A new expression can be created using the {@link #create()} method. This method
 * will initialize an expression with an empty condition that will match any
 * request.
 *<p>
 * Conditions are added to the expression by chaining method invocations and form
 * a logical <b>AND</b> expression. Each method invocation will return a different
 * instance representing the last condition in the expression. Each instance can
 * represent only one condition, invoking a method representing a condition more
 * than once per instance will throw an {@link IllegalStateException}.
 *<p>
 * The expression can be evaluated against a request using the
 * {@link #test(Context)} method
 *<p>
 * The {@link #thenNext()} method can be invoked on an expression to create
 * a {@link Handler}.
 *<p>
 */
@SuppressWarnings("unused")
public final class ContextPredicate implements ContextUser, ContextFail {

    /**
     * A {@link Handler} that conditionally delegates to other {@link Handler}
     * instances based on a {@link ContextPredicate}.
     * There can be at most 2 handlers: a required one for matched requests
     * and an optional one for requests that are not matched.
     */
    public static class ConditionalHandler implements Handler, ContextFail {
        /**
         * The condition for the delegation.
         */
        private final ContextPredicate condition;

        /**
         * The {@link Handler} to use when the predicate matches.
         */
        private final Handler acceptHandler;

        /**
         * The {@link Handler} to use when the predicate does not match.
         */
        private Handler declineHandler;

        /**
         * Create a new instance.
         * @param condition the predicate
         * @param acceptHandler the predicate to use when the predicate matches
         * @param declineHandler the predicate to use when the predicate does not match.
         */
        private ConditionalHandler(final ContextPredicate condition,
                                   final Handler acceptHandler, final Handler declineHandler) {
            this.condition = condition;
            this.acceptHandler = acceptHandler == null ? ctx -> {} : acceptHandler;
            this.declineHandler = declineHandler == null
                    ? (ctx -> condition.fail(HttpStatus.BAD_REQUEST)) : declineHandler;
        }

        /**
         * Set the {@link Handler} to use when the predicate does not match the
         * request.
         *
         * @param declineHandler handler to use when the predicate does not match
         * @return created {@link Handler}
         */
        public Handler otherwise(final Handler declineHandler) {
            this.declineHandler = declineHandler;
            return this;
        }

        public Handler otherwise(HttpResponseException responseException) {
            this.declineHandler = ctx -> fail(ctx, responseException);
            return this;
        }

        public Handler otherwise(int status, String message) {
            this.declineHandler = ctx -> fail(ctx, status, message);
            return this;
        }

        public Handler otherwise(HttpStatus httpStatus) {
            this.declineHandler = ctx -> fail(httpStatus);
            return this;
        }

        @Override
        public void handle(@NotNull Context ctx) throws Exception {
            if (condition.test(ctx)) {
                acceptHandler.handle(ctx);
            } else {
                declineHandler.handle(ctx);
            }
        }
    }

    /**
     * Creates new empty {@link ContextPredicate} instance.
     * @return new empty predicate (accepts all requests).
     */
    public static ContextPredicate create() {
        return new ContextPredicate();
    }

    /**
     * Recursive evaluation of a predicate chain.
     * @param currentValue the initial value
     * @param predicate the predicate to resolve the new value
     * @param ctx Provides access to functions for handling the request and response
     * @return the evaluated value
     */
    private static boolean eval(final boolean currentValue,
                                final ContextPredicate predicate, final Context ctx){

        boolean newValue = predicate.condition.eval(currentValue, ctx);
        if (predicate.next != null) {
            return eval(newValue, predicate.next, ctx);
        }
        return newValue;
    }

    /**
     * A condition that returns the current value.
     */
    private static final Condition EMPTY_CONDITION = (a, b) -> a;

    /**
     * The first predicate in the predicate chain.
     */
    private final ContextPredicate first;

    /**
     * The next predicate in the predicate chain.
     */
    private volatile ContextPredicate next;

    /**
     * The condition for this predicate.
     */
    private final Condition condition;

    /**
     * Create an empty predicate.
     */
    private ContextPredicate(){
        this.first = this;
        this.next = null;
        this.condition = EMPTY_CONDITION;
    }

    /**
     * Create a composed predicate with the given condition.
     * @param first the first predicate in the chain
     * @param cond the condition for the new predicate
     */
    private ContextPredicate(final ContextPredicate first,
                             final Condition cond){
        this.first = first;
        this.next = null;
        this.condition = cond;
    }

    public ConditionalHandler thenNext(Handler successHandler) {
        return new ConditionalHandler(this, successHandler, null);
    }

    public ConditionalHandler thenNext() {
        return new ConditionalHandler(this, null, null);
    }

    /**
     *  Accept requests only when the user is authenticated
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the authenticated predicate
     */
    public ContextPredicate isAuthenticated() {
        return and(this::isAuthenticated);
    }

    /**
     *  Accept requests only when the user has all the specified roles
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the role predicate
     */
    public ContextPredicate hasRoles(RouteRole... routeRoles) {
        return and(ctx -> hasRoles(ctx, routeRoles));
    }

    /**
     *  Accept requests only when the user has any of the specified roles
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the role predicate
     */
    public ContextPredicate hasAnyRole(RouteRole... routeRoles) {
        return and(ctx -> hasAnyRole(ctx, routeRoles));
    }

    /**
     * Create a composed predicate and add it in the predicate chain.
     * @param newCondition the condition for the new predicate
     * @throws IllegalStateException if the next condition is already set
     * @return the created predicate
     */
    private ContextPredicate nextCondition(final Condition newCondition){
        if (next != null) {
            throw new IllegalStateException("next predicate already set");
        }
        this.next = new ContextPredicate(this.first, newCondition);
        return this.next;
    }

    /**
     * Evaluate this predicate.
     * @param ctx Provides access to functions for handling the request and response
     * @return the computed value
     */
    boolean test(final Context ctx) {
        return eval(/* initial value */ true, this.first, ctx);
    }

    /**
     * Returns a composed predicate that represents a logical AND expression
     * between this predicate and another predicate.
     *
     * @param predicate predicate to compose with
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     */
    public ContextPredicate and(final Predicate<Context> predicate) {
        return nextCondition((exprVal, ctx) -> exprVal && predicate.test(ctx));
    }

    /**
     * Returns a composed predicate that represents a logical OR expression
     * between this predicate and another predicate.
     *
     * @param predicate predicate that compute the new value
     * @return composed predicate representing the logical expression between
     * this predicate <b>OR</b> the provided predicate
     */
    public ContextPredicate or(final Predicate<Context> predicate) {
        return nextCondition((exprVal, ctx) -> exprVal || predicate.test(ctx));
    }

    /**
     * Return a predicate that represents the logical negation of this predicate.
     * @return new predicate that represents the logical negation of this predicate.
     */
    public ContextPredicate negate() {
        return nextCondition((exprVal, ctx) -> !exprVal);
    }

    /**
     * Accepts only requests with one of specified HTTP methods.
     *
     * @param methods Acceptable method names
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified methods array is null
     */
    public ContextPredicate isOfMethod(final String... methods) {
        Objects.requireNonNull(methods, "methods");
        return and(ctx -> Stream.of(methods)
                .map(String::toUpperCase)
                .anyMatch(ctx.method().name()::equals));
    }

    /**
     * Accepts only requests with one of specified HTTP methods.
     *
     * @param methods Acceptable method names
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the methods type array is null
     */
    public ContextPredicate isOfMethod(final HandlerType... methods) {
        Objects.requireNonNull(methods, "methods");
        return and(ctx -> Stream.of(methods)
                .map(HandlerType::name)
                .anyMatch(ctx.method().name()::equals));
    }

    /**
     * Accept requests only when the specified header name exists.
     *
     * @param name header name
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified name is null
     */
    public ContextPredicate containsHeader(final String name) {
        return containsHeader(name, (c) -> true);
    }

    /**
     * Accept requests only when the specified header contains a given value.
     * <p>
     * If the request contains more than one header, it will be accepted
     * if <b>any</b> of the values is equal to the provided value.
     *
     * @param name header name
     * @param value the expected header value
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified name or value is null
     */
    public ContextPredicate containsHeader(final String name,
                                           final String value) {

        Objects.requireNonNull(value, "header value");
        return containsHeader(name, value::equals);
    }

    /**
     * Accept requests only when the specified header is valid.
     * A header is valid when the supplied predicate matches the header value.
     * If the request contains more than one header, it will be accepted if the
     * predicate matches <b>any</b> of the values.
     *
     * @param name header name
     * @param predicate predicate to match the header value
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified name or predicate is null
     */
    public ContextPredicate containsHeader(final String name,
                                           final Predicate<String> predicate) {

        Objects.requireNonNull(name, "header name");
        Objects.requireNonNull(predicate, "header predicate");

        return and(ctx -> ctx.headerMap().entrySet().stream()
                .filter(e -> e.getKey().equals(name))
                .map(Map.Entry::getValue)
                .anyMatch(predicate));
    }

    /**
     * Accept requests only when the specified query parameter exists.
     *
     * @param name query parameter name
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified name is null
     */
    public ContextPredicate containsQueryParameter(final String name) {
        return containsQueryParameter(name, (c) -> true);
    }

    /**
     * Accept requests only when the specified query parameter contains a given
     * value.
     *
     * @param name query parameter name
     * @param value expected query parameter value
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified name or value is null
     */
    public ContextPredicate containsQueryParameter(final String name,
                                                   final String value) {

        Objects.requireNonNull(value, "query param value");
        return containsQueryParameter(name, value::equals);
    }

    /**
     * Accept requests only when the specified query parameter is valid.
     *
     * @param name query parameter name
     * @param predicate to match the query parameter value
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified name or predicate is null
     */
    public ContextPredicate containsQueryParameter(final String name,
                                                   final Predicate<String> predicate) {
        Objects.requireNonNull(name, "query param name");
        Objects.requireNonNull(predicate, "query param predicate");

        return and(ctx -> ctx.queryParamMap().entrySet().stream()
                .filter(entry -> entry.getKey().equals(name))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .anyMatch(predicate));
    }

    /**
     * Accept request only when the specified cookie exists.
     *
     * @param name cookie name
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified name is null
     */
    public ContextPredicate containsCookie(final String name) {
        return containsCookie(name, (c) -> true);
    }

    /**
     * Accept requests only when the specified cookie contains a given value.
     *
     * @param name cookie name
     * @param value expected cookie value
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified name or value is null
     */
    public ContextPredicate containsCookie(final String name,
                                           final String value) {
        Objects.requireNonNull(value, "cookie value");
        return containsCookie(name, value::equals);
    }

    /**
     * Accept requests only when the specified cookie is valid.
     *
     * @param name cookie name
     * @param predicate predicate to match the cookie value
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified name or predicate is null
     */
    public ContextPredicate containsCookie(final String name,
                                           final Predicate<String> predicate) {
        Objects.requireNonNull(name, "cookie name");
        Objects.requireNonNull(predicate, "cookie predicate");
        return and(ctx -> ctx.cookieMap().entrySet().stream()
                .filter(entry -> entry.getKey().equals(name))
                .map(Map.Entry::getValue)
                .anyMatch(predicate));
    }

    /**
     * Accept requests only when it accepts any of the given content types.
     *
     * @param contentType the content types to test
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified content type array is null
     */
    public ContextPredicate accepts(final String... contentType) {
        Objects.requireNonNull(contentType, "content types");
        return and(ctx -> Negotiate.matches(ctx, contentType));
    }

    /**
     * Only accept request that accepts any of the given content types.
     *
     * @param contentType the content types to test
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified content type array is null
     */
    public ContextPredicate accepts(final MediaType... contentType) {
        Objects.requireNonNull(contentType, "accepted media types");
        return and(ctx -> Negotiate.matches(ctx, contentType));
    }

    /**
     * Only accept requests with any of the given content types.
     *
     * @param contentType Content type
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified content type array is null
     */
    public ContextPredicate hasContentType(final String... contentType) {
        Objects.requireNonNull(contentType, "accepted media types");
        return and(ctx -> {
            Optional<MediaType> actualContentType = ctx.contentType() == null
                    ? Optional.empty()
                    : Optional.of(MediaType.parse(ctx.contentType()));
            return actualContentType.isPresent()
                    && Stream.of(contentType)
                    .anyMatch((mt) -> actualContentType.get()
                            .equals(MediaType.parse(mt)));
        });
    }

    /**
     * Only accept requests with any of the given content types.
     *
     * @param contentType Content type
     * @return composed predicate representing the logical expression between
     * this predicate <b>AND</b> the provided predicate
     * @throws NullPointerException if the specified content type array is null
     */
    public ContextPredicate hasContentType(final MediaType... contentType) {
        Objects.requireNonNull(contentType, "content types");
        return and(ctx -> {
            Optional<MediaType> actualContentType = ctx.contentType() == null
                    ? Optional.empty()
                    : Optional.of(MediaType.parse(ctx.contentType()));
            return actualContentType.isPresent()
                    && Stream.of(contentType)
                    .anyMatch((mt) -> actualContentType.get()
                            .equals(mt));
        });
    }

    /**
     * A condition represents some logic that evaluates a {@code boolean}
     * value based on a current {@code boolean} value and an input object.
     */
    @FunctionalInterface
    private interface Condition {
        /**
         * Evaluate this condition as part of a logical expression.
         * @param currentValue the current value of the expression
         * @param ctx Provides access to functions for handling the request and response
         * @return the new value
         */
        boolean eval(boolean currentValue, Context ctx);
    }
}
