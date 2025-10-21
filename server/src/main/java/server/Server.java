package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import service.UserService;

import java.util.Map;

public class Server {

    private final Javalin server;
    private UserService userService;
    private DataAccess dataAccess;

    public Server() {
        this.dataAccess = new MemoryDataAccess();
        this.userService = new UserService(dataAccess);

        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.post("user", this::register);
        server.post("session", this::login);
        server.delete("session", this::logout);
        server.delete("db", this::clear);
        server.post("game", this::createGame);
        // Register your endpoints and exception handlers here.

    }

    private void createGame(@NotNull Context ctx) {
        var serializer = new Gson();
        var reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, UserData.class);

        try {
            var res = userService.register(req);
            ctx.result(serializer.toJson(res));
            ctx.status(200);
        } catch (DataAccessException e) {
            var errorJson = serializer.toJson(Map.of("message", e.getMessage()));
            ctx.result(errorJson);
            //System.out.println(e.getMessage());
            if (e.getMessage().equals("Error: Bad Request")) {
                ctx.status(400);
            } else {
                ctx.status(403);
            }
        }
    }

    private void clear(@NotNull Context ctx) {
        try {
            userService.clear();
            ctx.result("{}");
            ctx.status(200);
        } catch (DataAccessException e) {
            ctx.status(500);
        }
    }

    private void register(Context ctx) {
        var serializer = new Gson();
        var reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, UserData.class);

        try {
            var res = userService.register(req);
            ctx.result(serializer.toJson(res));
            ctx.status(200);
        } catch (DataAccessException e) {
            var errorJson = serializer.toJson(Map.of("message", e.getMessage()));
            ctx.result(errorJson);
            //System.out.println(e.getMessage());
            if (e.getMessage().equals("Error: Bad Request")) {
                ctx.status(400);
            } else {
                ctx.status(403);
            }
        }
    }

    private void login(Context ctx) {
        var serializer = new Gson();
        var reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, UserData.class);

        try {
            var res = userService.login(req);
            ctx.result(serializer.toJson(res));
            ctx.status(200);
        } catch (DataAccessException e){
            var errorJson = serializer.toJson(Map.of("message", e.getMessage()));
            ctx.result(errorJson);
            //System.out.println(e.getMessage());
            if (e.getMessage().equals("Error: Unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(400);
            }
        }
    }

    private void logout(Context ctx) {
        var serializer = new Gson();

        try {
            var authToken = ctx.header("authorization");
            if (authToken == null) {
                System.out.println("authToken is null");
                System.out.println(ctx.headerMap());
            }

            userService.logout(authToken);
            ctx.result("{}");
            ctx.status(200);
        } catch (DataAccessException e){
            var errorJson = serializer.toJson(Map.of("message", e.getMessage()));
            ctx.result(errorJson);
            //System.out.println(e.getMessage());
            ctx.status(401);
        }
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
