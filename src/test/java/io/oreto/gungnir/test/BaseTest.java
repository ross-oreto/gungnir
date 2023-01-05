package io.oreto.gungnir.test;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import io.oreto.gungnir.app.Gungnir;
import io.oreto.gungnir.http.MediaType;
import jodd.http.Cookie;
import jodd.http.HttpRequest;

import java.util.Arrays;

public class BaseTest {
    protected static Gungnir app;

    protected static Config serverConfig;

    protected static Gson gson = GsonMapper.gson;

    public static void init() {
        System.setProperty("gungnir.profiles", "test");
        app = new TestApp();
        serverConfig = app.conf().getConfig("server");
        app.start();
    }

    protected HttpRequest request(String method, String uri, MediaType accept) {
        return new HttpRequest()
                .method(method)
                .protocol("http")
                .host(serverConfig.getString("host"))
                .port(serverConfig.getInt("port"))
                .accept(accept.toString())
                .path(uri);
    }

    protected HttpRequest request(String method, String uri) {
       return request(method, uri, MediaType.APPLICATION_JSON);
    }

    protected Cookie login() {
        return Arrays.stream(getHtml("login").send().cookies())
                .filter(cookie -> cookie.getName().contains("JSESSIONID"))
                .findFirst()
                .orElse(null);
    }

    protected HttpRequest getRequest(String uri) {
        return request("GET", uri);
    }

    protected HttpRequest getHtml(String uri) {
        return request("GET", uri, MediaType.TEXT_HTML);
    }
}
