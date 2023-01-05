package io.oreto.gungnir.app;

public class ServiceRegistrar {

    private final Gungnir gungnir;

    public ServiceRegistrar(Gungnir gungnir) {
        this.gungnir = gungnir;
    }

    public ServiceRegistrar register(Service service) {
        gungnir.register(service);
        return this;
    }
}
