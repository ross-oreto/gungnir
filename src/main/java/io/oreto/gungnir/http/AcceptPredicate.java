package io.oreto.gungnir.http;

import java.util.function.Predicate;

/**
 * API to model HTTP content negotiation using {@code Accept-*} request headers. (RFC 7231 and RFC 2295)
 * <p>
 * It extends {@link Predicate} for smooth integration with standard functional APIs.
 *
 * @param <T> The type of the <i>Accept-*</i> header value.
 */
interface AcceptPredicate<T> extends Predicate<T> {
    /**
     * The media type quality factor ({QUALITY_FACTOR_PARAMETER}) parameter name.
     */
    String QUALITY_FACTOR_PARAMETER = "q";

    /**
     * The wildcard value {#WILDCARD_VALUE} used by standard in several headers.
     */
    String WILDCARD_VALUE = "*";

    /**
     * Gets quality factor parameter ({@value QUALITY_FACTOR_PARAMETER}) as a double value. If missing, then returns {@code 1.0}
     * @return Quality factor parameter.
     */
    double qualityFactor();
}
