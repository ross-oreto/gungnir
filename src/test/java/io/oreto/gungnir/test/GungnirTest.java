package io.oreto.gungnir.test;

import com.google.gson.reflect.TypeToken;
import io.javalin.http.HandlerType;
import io.oreto.gungnir.security.User;
import io.oreto.gungnir.security.UserImpl;
import jodd.http.Cookie;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.oreto.gungnir.info.InfoService.JAVA_VER_PROP;
import static io.oreto.gungnir.info.InfoService.getJavaVersion;
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
        assertEquals(gson.toJson(app.getRoutes()), getRequest("info/routes").send().bodyRaw());
        assertEquals(
                app.getRoutes().stream().filter(r -> r.getPath().startsWith("/info")).toList().size()
                , gson.fromJson(getRequest("info/routes/info").send().bodyRaw(), List.class).size()
        );
        Cookie sessionCookie = login();

        assertNotNull(sessionCookie);
        assertFalse(sessionCookie.getValue().isEmpty());

        User user = gson.fromJson(getRequest("info/user").cookies(sessionCookie).send().bodyRaw(), UserImpl.class);
        assertEquals("Bilbo", user.getFirstName());

        Map<String, Object> info = gson.fromJson(getRequest("info").send().bodyRaw(), new TypeToken<Map<String, Object>>() {}.getType());
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
        assertEquals(gson.toJson(app.getRoutes()), getRequest("info/routes").send().bodyRaw());
    }
}
