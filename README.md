### Gungnir
In Norse mythology, Gungnir is the spear of the god Odin.

### benefits
- Gungnir extends Javalin providing more power and functionality to quickly bootstrap an application
- Service classes provide encapsulated routing rules and related logic
- Provides fluent route handlers supporting multiple filter chaining including a powerful context predicate builder
- Easy content negotiation built in
- Easy authentication/login implementation
- profile/environment detection
- provides config using Lightbend config
  - Why provide a concrete implementation and not an abstraction such as microprofile?
  - Lightbend config supports most major properties format. .properties, .json, .conf. 
  - yaml is left out, however yaml is not as prominent as the other formats and the most recent yaml versions have security vulnerabilities which seems like a double wammy considering the format isn't as powerful as hocon. 
  - Lightbend config is a small library with zero dependencies. This is hugely attractive considering what the library can accomplish.
- easy CORS, static files, and SPA configuration using config properties

### Create a new app
``` 
public class DemoApp extends Gungnir {

}
```

start the app
```
new DemoApp().start(); 
```


### Registering a service
``` 
    @Override
    protected void registerServices(ServiceRegistrar registrar) {
        registrar
                .register(new AppService());
    }
   
    // AppService.java 
    @Override
    public void routing(Router router) {
        router.routes(() -> path(uri("app"), () -> {
            post(uri("stop"), stop());
            post(uri("restart"), restart());
        }));
    }
```

### Predicate Builder
continue if request has application/json accept header
```
ContextPredicate.create()
  .accepts(MediaType.APPLICATION_JSON)
  .thenNext()
  .otherwise(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
```

make sure the request is authenticated
```
ContextPredicate.create()
  .isAuthenticated()
  .thenNext()
  .otherwise(new UnauthorizedResponse());
```

authorize using roles
```
ContextPredicate.create()
  .hasRoles(Role.of("ADMIN"))
  .or(ctx -> hasAnyRole(ctx, Role.of("CAPTAIN"), Role.of("PLANET")))
  .thenNext()
  .otherwise(new ForbiddenResponse());
```

Chain multiple predicates together
```
router.get(uri()
       , ContextPredicate.create()
               .accepts(MediaType.APPLICATION_JSON)
               .thenNext()
               .otherwise(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        , ContextPredicate.create()
                .isAuthenticated()
                .thenNext()
                .otherwise(new UnauthorizedResponse())
        , ContextPredicate.create()
                .hasRoles(Role.of("ADMIN"))
                .or(ctx -> hasAnyRole(ctx, Role.of("CAPTAIN"), Role.of("PLANET")))
                .thenNext()
                .otherwise(new ForbiddenResponse())
       , ctx -> ctx.json(getData())
);
```

### Content Negotiation
```
Negotiate.create()
        .html(ctx -> ctx.render("index.jte", model))
        .json(ctx -> ctx.json(model))
        .otherwise(ctx -> fail(HttpStatus.UNSUPPORTED_MEDIA_TYPE));
```