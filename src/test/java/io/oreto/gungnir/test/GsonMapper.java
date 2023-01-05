package io.oreto.gungnir.test;

import com.google.gson.*;
import io.javalin.json.JsonMapper;
import io.javalin.security.RouteRole;
import io.oreto.gungnir.security.Role;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public interface GsonMapper extends JsonMapper {
    JsonMapper mapper = new GsonMapper() {};

    JsonSerializer<RouteRole> roleSer = (src, typeOfSrc, context) -> new JsonPrimitive(src.toString());
    JsonDeserializer<RouteRole> roleDes = (json, typeOfT, context) -> Role.of(json.getAsString());

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(RouteRole.class, roleSer)
            .registerTypeAdapter(RouteRole.class, roleDes)
            .create();

    @NotNull
    @Override
    default String toJsonString(@NotNull Object obj, @NotNull Type type) {
        return gson.toJson(obj, type);
    }

    @NotNull
    @Override
    default <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
        return gson.fromJson(json, targetType);
    }
}
