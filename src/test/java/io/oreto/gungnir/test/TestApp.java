package io.oreto.gungnir.test;


import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.json.JsonMapper;
import io.oreto.gungnir.app.AppService;
import io.oreto.gungnir.app.Gungnir;
import io.oreto.gungnir.app.ServiceRegistrar;
import io.oreto.gungnir.render.ViewRenderer;
import io.oreto.gungnir.security.Authenticator;
import io.oreto.gungnir.security.LoginService;
import io.oreto.gungnir.security.Role;
import io.oreto.gungnir.security.UserImpl;
import io.oreto.gungnir.test.data.DataRepo;
import io.oreto.gungnir.test.data.DataService;

import java.util.Optional;

import static io.oreto.gungnir.app.Configurable.loadConfig;

public class TestApp extends Gungnir {
    public TestApp() {
        super(loadConfig("test"));
    }

    @Override
    protected JsonMapper jsonMapper() {
        return GsonMapper.mapper;
    }

    @Override
    protected ViewRenderer viewRenderer() {
        return new JteRenderer(conf());
    }

    @Override
    protected void registerServices(ServiceRegistrar registrar) {
        registrar
                .register(new AppService())
                .register(new DataService(new DataRepo()))
                .register(new LoginService(ctx -> Optional.of(
                        isAuthenticated(ctx) ? getUser(ctx) : new UserImpl("bilbo.baggins")
                                .withFirstName("Bilbo")
                                .withLastName("Baggins")
                                .withRoles(Role.of("burglar"))
                                .withToken(Authenticator.generateToken())
                )) {
                    @Override
                    protected Handler handleLoginSuccess() {
                        return ctx -> ctx.json(getUser(ctx));
                    }

                    @Override
                    protected Handler handleLoginFailure() {
                        return ctx -> ctx.status(HttpStatus.UNAUTHORIZED);
                    }

                    @Override
                    public String name() {
                        return LoginService.class.getName();
                    }
                });
    }
}
