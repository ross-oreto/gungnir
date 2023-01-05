package io.oreto.gungnir.app;

/**
 * Provides a fluent api object that just registers services one at a time.
 * Useful to encapsulate with a method which registers all services for the app.
 */
public class ServiceRegistrar {

    private final Gungnir gungnir;

    public ServiceRegistrar(Gungnir gungnir) {
        this.gungnir = gungnir;
    }

    /**
     * Register a service with gungnir
     * @param service The serviec to register
     * @return this registrar
     */
    public ServiceRegistrar register(Service service) {
        gungnir.register(service);
        return this;
    }
}
