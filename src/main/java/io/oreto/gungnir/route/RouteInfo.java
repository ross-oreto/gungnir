package io.oreto.gungnir.route;

import io.javalin.event.HandlerMetaInfo;
import io.javalin.event.WsHandlerMetaInfo;
import io.javalin.security.RouteRole;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Info for a particular route
 *  group: which name/class/service class the route belongs to
 * method: The http method to access the route
 *   path: The uri of the route
 *  roles: Roles needed to the access route if any
 */
public class RouteInfo {
    public static RouteInfo of(HandlerMetaInfo metaInfo) {
        return new RouteInfo(metaInfo);
    }

    public static RouteInfo of(WsHandlerMetaInfo metaInfo) {
        return new RouteInfo(metaInfo);
    }

    private final String group;

    private final String method;
    private final String path;
    private final Set<String> roles;

    protected RouteInfo(HandlerMetaInfo metaInfo) {
        this.group = metaInfo.getHandler().getClass().getName().split("\\$\\$")[0];
        this.method = metaInfo.getHttpMethod().name();
        this.path = metaInfo.getPath();
        this.roles = metaInfo.getRoles().stream().map(RouteRole::toString).collect(Collectors.toSet());
    }

    protected RouteInfo(WsHandlerMetaInfo metaInfo) {
        this.group = metaInfo.getHandler().getClass().getName().split("\\$\\$")[0];
        this.method = "WS";
        this.path = metaInfo.getPath();
        this.roles = metaInfo.getRoles().stream().map(RouteRole::toString).collect(Collectors.toSet());
    }

    public String getGroup() {
        return group;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteInfo routeInfo = (RouteInfo) o;
        return method.equals(routeInfo.method) && path.equals(routeInfo.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path);
    }
}
