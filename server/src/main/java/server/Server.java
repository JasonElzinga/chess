package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.model.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import service.UserService;

public class Server {

    private final Javalin server;
    private UserService userService;
    private DataAccess dataAccess;

    public Server() {
        userService = new UserService();
        dataAccess = new MemoryDataAccess(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));
        server.delete("db", ctx -> ctx.result("{}"));

        server.post("user", this::register);
        // Register your endpoints and exception handlers here.

    }


    private void register(Context cxt) {
        var serializer = new Gson();
        String reqJson = cxt.body();
        var req = serializer.fromJson(reqJson, UserData.class);
        //req.put("authToken", "cow");
        //var user = req.username;


        var res = userService.register(req);
        cxt.result(serializer.toJson(res));
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
