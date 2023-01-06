package io.oreto.gungnir.security;

import io.javalin.security.RouteRole;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * Provides an abstraction for a user role which can implements a character sequence, being the name
 * , and can be serialized
 */
public abstract class RoleSequence implements RouteRole, Serializable, CharSequence {
    private final String name;

    protected RoleSequence(String name) {
        this.name = name;
    }

    @Override
    public int length() {
        return name.length();
    }

    @Override
    public char charAt(int index) {
        return name.charAt(index);
    }

    @NotNull
    @Override
    public CharSequence subSequence(int start, int end) {
        return name.subSequence(start, end);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleSequence role = (RoleSequence) o;
        return Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
