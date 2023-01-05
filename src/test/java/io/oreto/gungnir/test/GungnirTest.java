package io.oreto.gungnir.test;

import com.google.gson.reflect.TypeToken;
import io.javalin.http.HandlerType;
import io.oreto.gungnir.app.AppService;
import io.oreto.gungnir.route.RouteInfo;
import io.oreto.gungnir.security.User;
import io.oreto.gungnir.security.UserImpl;
import jodd.http.Cookie;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.oreto.gungnir.app.AppService.JAVA_VER_PROP;
import static io.oreto.gungnir.app.AppService.getJavaVersion;
import static org.junit.jupiter.api.Assertions.*;

public class GungnirTest extends BaseTest {
    @BeforeAll
    public static void init() {
        System.setProperty("gungnir.profiles", "test");
        app = new TestApp();
        serverConfig = app.conf().getConfig("server");
        app = app.start();
    }

    @AfterAll
    public static void cleanup() {
    }

    @Test
    public void startupTest() {
        assertTrue(app.isTest());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void infoTest() {
        assertEquals(gson.toJson(app.getRoutes()), getRequest("app/info/routes").send().bodyRaw());
        Collection<RouteInfo> test = gson.fromJson(getRequest("app/info/routes/" + AppService.class.getName()).send().bodyRaw(),new TypeToken<Collection<RouteInfo>>() {}.getType());

        assertEquals(app.getRoutes(AppService.class.getName()), test);
        Cookie sessionCookie = login();

        assertNotNull(sessionCookie);
        assertFalse(sessionCookie.getValue().isEmpty());

        User user = gson.fromJson(getRequest("app/info/user").cookies(sessionCookie).send().bodyRaw(), UserImpl.class);
        assertEquals("Bilbo", user.getFirstName());

        Map<String, Object> info = gson.fromJson(getRequest("app/info/env").send().bodyRaw(), new TypeToken<Map<String, Object>>() {}.getType());
        assertEquals(getJavaVersion(), info.get(JAVA_VER_PROP));
        assertArrayEquals(app.getProfiles(), ((List<String>)info.get("profiles")).toArray(String[]::new));
    }

    @Test
    public void dataTest() {
        assertEquals(gson.toJson(List.of("a", "b", "c")), getRequest("/").send().bodyRaw());
    }

    @Test
    public void restartTest() {
        assertEquals("issued server restart", request(HandlerType.POST.name(), "app/restart").send().bodyRaw());
        assertEquals(gson.toJson(app.getRoutes()), getRequest("app/info/routes").send().bodyRaw());
    }
}
