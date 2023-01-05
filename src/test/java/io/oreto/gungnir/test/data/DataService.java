package io.oreto.gungnir.test.data;

import io.javalin.http.*;
import io.oreto.gungnir.http.ContextPredicate;
import io.oreto.gungnir.app.Service;
import io.oreto.gungnir.http.MediaType;
import io.oreto.gungnir.route.Router;

import java.util.List;

public class DataService extends Service {

    //    public static void main(String[] args) {
    //        new StageApp()
    //                .get("/", ctx -> ctx.render("index.jte"), Roles.of("burglar"))
    //                .get("/test", Negotiate.create().html_fallback_json("test.jte", Map.of("s", "turbo")), Roles.of("burglar"))
    //                .start();
    //    }
    private final IRepo dataRepo;

    public DataService(IRepo dataRepo) {
        this.dataRepo = dataRepo;
    }

    public List<String> getData() {
        return dataRepo.list();
    }

    @Override
    public void routing(Router router) {
        router.get(uri()
               , ContextPredicate.create()
                        .accepts(MediaType.APPLICATION_JSON)
                        .thenNext()
                        .otherwise(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
               , ctx -> ctx.json(getData())
        );
    }

}
