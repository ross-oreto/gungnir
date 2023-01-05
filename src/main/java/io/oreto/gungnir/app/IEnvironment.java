package io.oreto.gungnir.app;

public interface IEnvironment {
    /**
     * Determine the environment profiles to load
     * @return The application profiles which are defined in the System property gungnir.profiles, default to dev
     */
    static String[] loadProfiles() {
        return System.getProperty("gungnir.profiles", Gungnir.Profile.dev.name()).split(",");
    }

    /**
     * Get the active profiles for the environment
     * @return All the active profiles
     */
    String[] getProfiles();

    /**
     * Returns true if the specified profiles are all active
     * @param profiles The profiles to check
     * @return true if the specified profiles are all active, false otherwise
     */
    default boolean acceptsProfiles(String... profiles) {
        for (String profile : getProfiles()) {
            for (String name : profiles) {
                if (!name.trim().equals(profile.trim()))
                    return false;
            }
        }
        return true;
    }

    /**
     * Determine if in dev
     * @return True if the dev profile is active
     */
    default boolean isDev() {
        return acceptsProfiles(Gungnir.Profile.dev.name());
    }

    /**
     * Determine if in test
     * @return True if the test profile is active
     */
    default boolean isTest() {
        return acceptsProfiles(Gungnir.Profile.test.name());
    }
}
