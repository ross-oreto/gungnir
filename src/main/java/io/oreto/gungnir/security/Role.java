package io.oreto.gungnir.security;

import java.io.Serial;

public class Role extends RoleSequence {
    @Serial
    private static final long serialVersionUID = 1932L;

    public static Role of(String name) {
        return new Role(name);
    }

    protected Role(String name) {
        super(name);
    }
}
