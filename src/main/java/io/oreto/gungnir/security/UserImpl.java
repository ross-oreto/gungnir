package io.oreto.gungnir.security;

import io.javalin.security.RouteRole;

import java.io.Serial;
import java.util.*;

public class UserImpl implements User {
    @Serial
    private static final long serialVersionUID = 1931L;

    private final String subject;
    private final Set<RouteRole> roles;
    private final Map<String, Object> attributes;

    public UserImpl(String username) {
        this.subject = username;
        this.roles = new HashSet<>();
        this.attributes = new HashMap<>();
        attributes.put(User.username, username);
    }

    /**
     * The user subject. Usually a human representation that identifies this user
     *
     * @return The subject for this user or null
     */
    @Override
    public String getSubject() {
        return subject;
    }

    /**
     * Retrieve user roles
     *
     * @return Set of user roles
     */
    @Override
    public Set<RouteRole> getRoles() {
        return roles;
    }

    /**
     * Gets extra attributes of the user.
     *
     * @return A map with any relevant attributes.
     */
    @Override
    public Map<String, Object> attributes() {
        return attributes;
    }

    public UserImpl withRoles(Collection<RouteRole> roles) {
        this.roles.addAll(roles);
        return this;
    }

    public UserImpl withRoles(RouteRole... roles) {
        this.roles.addAll(List.of(roles));
        return this;
    }

    public UserImpl withFirstName(String firstName) {
        attributes.put(User.firstName, firstName);
        return this;
    }

    public UserImpl withLastName(String firstName) {
        attributes.put(User.lastName, firstName);
        return this;
    }

    public UserImpl withEmail(String email) {
        attributes.put(User.email, email);
        return this;
    }

    public UserImpl withToken(String token) {
        if (Objects.nonNull(token) && !token.isBlank())
            attributes.put(User.accessToken, token);
        return this;
    }
}
