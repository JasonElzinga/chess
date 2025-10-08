package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.json.JavalinGson;

import java.util.Map;

public class Server {

    private final Javalin server;

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));
        server.delete("db", ctx -> ctx.result("{}"));

        server.post("user", this::register);
        // Register your endpoints and exception handlers here.

    }


    private void register(Context cxt) {
        var serializer = new Gson();
        var req = serializer.fromJson(cxt.body(), Map.class);
        req.put("authToken", "cow");
        //var user = req.username;
        var res = serializer.toJson(req);

        cxt.result(res);
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
