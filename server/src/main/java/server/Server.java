package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MySQLDataAccess;
import model.CreateGameRequest;
import model.JoinGameRequest;
import model.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import server.websocket.WebSocketHandler;
import service.UserService;

import java.sql.SQLException;
import java.util.Map;

public class Server {

    private final Javalin server;
    private UserService userService;
    private DataAccess dataAccess;
    private final WebSocketHandler webSocketHandler;

    public Server(WebSocketHandler webSocketHandler) {
        //this.dataAccess = new MemoryDataAccess();
        try {
            this.dataAccess = new MySQLDataAccess();
        } catch (SQLException | DataAccessException e) {
            System.err.println("Failed to start the mysql database");
        }
        //this.dataAccess = new MemoryDataAccess();
        this.userService = new UserService(dataAccess);

        server = Javalin.create(config -> config.staticFiles.add("web"));

        this.webSocketHandler = webSocketHandler;

        server.post("user", this::register);
        server.post("session", this::login);
        server.delete("session", this::logout);
        server.delete("db", this::clear);
        server.post("game", this::createGame);
        server.get("game", this::listGames);
        server.put("game", this::joinGame);

        server.ws("/ws", ws -> {
            ws.onConnect(webSocketHandler);
            ws.onMessage(webSocketHandler);
            ws.onClose(webSocketHandler);
        });

    }

    private void joinGame(@NotNull Context ctx) {
        var serializer = new Gson();
        var reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, JoinGameRequest.class);
        var authToken = ctx.header("authorization");

        try {
            userService.joinGame(req.playerColor(), req.gameID(), authToken);
            ctx.result("{}");
            ctx.status(200);
        } catch (DataAccessException e) {
            var errorJson = serializer.toJson(Map.of("message", e.getMessage()));
            ctx.result(errorJson);
            if (e.getMessage().equals("Error: already taken")) {
                ctx.status(403);
            }
            else if (e.getMessage().equals("Error: unauthorized")) {
                ctx.status(401);
            }
            else if (e.getMessage().equals("Error: bad request")){
                ctx.status(400);
            }
            else {
                ctx.status(500);
            }
        }
    }

    private void listGames(@NotNull Context ctx) {
        var serializer = new Gson();

        try {
            var res = userService.listGames(ctx.header("authorization"));
            ctx.result(serializer.toJson(res));
            ctx.status(200);
        } catch (DataAccessException e) {
            var errorJson = serializer.toJson(Map.of("message", e.getMessage()));
            ctx.result(errorJson);
            if (e.getMessage().equals("Error: unauthorized")){
                ctx.status(401);
            }
            else  {
                ctx.status(500);
            }
        }
    }

    private void createGame(@NotNull Context ctx) {
        var serializer = new Gson();
        var reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, CreateGameRequest.class);
        var authToken = ctx.header("authorization");

        try {
            var res = userService.createGame(req.gameName(), authToken);
            ctx.result(serializer.toJson(res));
            ctx.status(200);
        } catch (DataAccessException e) {
            var errorJson = serializer.toJson(Map.of("message", e.getMessage()));
            ctx.result(errorJson);
            //System.out.println(e.getMessage());
            if (e.getMessage().equals("Error: bad request")) {
                ctx.status(400);
            }
            else if (e.getMessage().equals("Error: unauthorized")){
                ctx.status(401);
            }
            else {
                ctx.status(500);
            }
        }
    }

    private void clear(@NotNull Context ctx) {
        var serializer = new Gson();
        try {
            userService.clear();
            ctx.result("{}");
            ctx.status(200);
        } catch (DataAccessException | SQLException e) {
            var errorJson = serializer.toJson(Map.of("message", e.getMessage()));
            ctx.result(errorJson);
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
            if (e.getMessage().equals("Error: bad request")) {
                ctx.status(400);
            }
            else if (e.getMessage().equals("Error: already taken")) {
                ctx.status(403);
            }
            else {
                ctx.status(500);
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
            if (e.getMessage().equals("Error: unauthorized")) {
                ctx.status(401);
            }
            else if (e.getMessage().equals("Error: database failure") || e.getMessage().equals("Error: failed to get connection")) {
                ctx.status(500);
            }
            else {
                ctx.status(400);
            }
        } catch (Exception e) {
            var errorJson = serializer.toJson(Map.of("message", "Error: database error"));
            ctx.result(errorJson);
            ctx.status(500);
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

            if (e.getMessage().equals("Error: database failure")) {
                ctx.status(500);
            }
            else {
                ctx.status(401);
            }
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
