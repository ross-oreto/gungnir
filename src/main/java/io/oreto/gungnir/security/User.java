package io.oreto.gungnir.security;


import io.javalin.security.RouteRole;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface User extends Serializable {
    String username = "username";
    String firstName = "firstName";
    String lastName = "lastName";
    String email = "email";

    String accessToken = "accessToken";

    Set<RouteRole> NO_ROLES = Set.of();
    User UN_AUTHENTICATED = new User() {
        @Override
        public Set<RouteRole> getRoles() {
            return NO_ROLES;
        }
        @Override
        public Map<String, Object> attributes() {
            return null;
        }
        @Override
        public boolean isAuthenticated() {
            return false;
        }
    };

    /**
     * The user subject. Usually a human representation that identifies this user
     * @return The subject for this user or null
     */
    default String getSubject() {
        return getUsername();
    }

    /**
     * Is the user authenticated
     * @return true if the user is authenticated, false otherwise
     */
    default boolean isAuthenticated() {
        return Objects.nonNull(getSubject());
    }

    /**
     * Retrieve user roles
     * @return Set of user roles
     */
    Set<RouteRole> getRoles();

    /**
     * Determine if user has the specified role
     * @param role The role to test
     * @return true if user has the specified role, false otherwise
     */
    default boolean hasRole(RouteRole role) {
        return getRoles().contains(role);
    }

    /**
     * Determine if user has all the specified roles
     * @param roles The roles to test
     * @return true if user has all the specified roles, false otherwise
     */
    default boolean hasRoles(RouteRole... roles) {
        return getRoles().containsAll(List.of(roles));
    }

    /**
     * Determine if user has all the specified roles
     * @param roles The roles to test
     * @return true if user has all the specified roles, false otherwise
     */
    default boolean hasRoles(Set<RouteRole> roles) {
        return getRoles().containsAll(roles);
    }

    /**
     * Determine if user has any of the specified roles
     * @param roles The roles to test
     * @return true if user has any of the specified roles, false otherwise
     */
    default boolean hasAnyRole(RouteRole... roles) {
        Set<RouteRole> userRoles = getRoles();
        for (RouteRole role : roles)
            if (userRoles.contains(role))
                return true;
        return false;
    }

    /**
     * Determine if user has any of the specified roles
     * @param roles The roles to test
     * @return true if user has any of the specified roles, false otherwise
     */
    default boolean hasAnyRole(Set<RouteRole> roles) {
        Set<RouteRole> userRoles = getRoles();
        for (RouteRole role : roles)
            if (userRoles.contains(role))
                return true;
        return false;
    }

    /**
     * Gets extra attributes of the user.
     * @return A map with any relevant attributes.
     */
    Map<String, Object> attributes();

    @SuppressWarnings("unchecked")
    default <T> T getAttribute(String key) {
        Map<String, Object> attributes = attributes();
        return attributes == null ? null : (T) attributes.get(key);
    }

    /**
     * Get the username
     * @return Username
     */
    default String getUsername() {
        return getAttribute(username);
    }

    /**
     * Get the user's first name
     * @return First Name
     */
    default String getFirstName() {
        return getAttribute(firstName);
    }

    /**
     * Get the user's last name
     * @return Last Name
     */
    default String getLastName() {
        return getAttribute(lastName);
    }

    /**
     * Get the user's primary email
     * @return user email
     */
    default String getEmail() {
        return getAttribute(email);
    }
}
