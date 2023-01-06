package io.oreto.gungnir.security;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a set of user roles
 */
public class Roles {
    private static final Set<Role> NO_ROLES = Set.of();

    /**
     * Returns empty set of roles
     * @return A set of roles with size 0
     */
    public static Roles empty() {
        return new Roles(NO_ROLES);
    }

    public static Roles of(Role... roles) {
        return new Roles(new HashSet<>(List.of(roles)));
    }

    public static Roles of(Collection<Role> roles) {
        return new Roles(new HashSet<>(roles));
    }

    public static Roles of(String... roles) {
        return new Roles(Arrays.stream(roles).map(Role::new).collect(Collectors.toSet()));
    }

    private final Set<Role> roles;

    protected Roles(Set<Role> roles) {
        this.roles = roles;
    }

    /**
     * Return the role set as a Role[]
     * @return An array of roles
     */
    public Role[] array() {
        return roles.toArray(Role[]::new);
    }

    public Set<Role> getRoles() {
        return roles;
    }
}
