package client;

import com.google.gson.Gson;
import model.*;

import java.io.ObjectStreamException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }


    public RegisterResponse register(UserData data) throws Exception {
        var json = new Gson().toJson(data);
        var req = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/user"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        var res = sendRequest(req);

        if (!isSuccessful(res.statusCode())) {
            throw new Exception("Logout failed: " + res.body());
        }

        return handleResponse(res, RegisterResponse.class);
//        var req = buildRequest("POST", "/user", data);
//        var res = sendRequest(req);
    }

    public void clear() throws Exception {
        var req = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/db"))
                .DELETE()
                .build();
        var res = sendRequest(req);
        sendRequest(req);
    }

    public void logout(String authToken) throws Exception {
        var req = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/session"))
                .header("authorization", authToken)
                .DELETE()
                .build();
        var res = sendRequest(req);

        if (!isSuccessful(res.statusCode())) {
            throw new Exception("Logout failed: " + res.body());
        }
    }

    public LoginResponse login(UserData data) throws Exception {
        var json = new Gson().toJson(data);

        var req = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/session"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        var res = sendRequest(req);

        if (!isSuccessful(res.statusCode())) {
            throw new Exception("Login failed: " + res.body());
        }
        return handleResponse(res, LoginResponse.class);
    }

    public ListGameResponse listGames(String authToken) throws Exception {

        var req = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game"))
                .header("authorization", authToken)
                .GET()
                .build();

        var res = sendRequest(req);

        if (!isSuccessful(res.statusCode())) {
            throw new Exception("Login failed: " + res.body());
        }
        return handleResponse(res, ListGameResponse.class);
    }

    public CreateGameResponse createGame(CreateGameRequest data, String authToken) throws Exception {
        var json = new Gson().toJson(data);

        var req = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game"))
                .header("authorization", authToken)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        var res = sendRequest(req);

        if (!isSuccessful(res.statusCode())) {
            throw new Exception("Login failed: " + res.body());
        }
        return handleResponse(res, CreateGameResponse.class);
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var req = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            req.setHeader("Content-Type", "application/json");
        }
        return req.build();
    }

    private HttpRequest.BodyPublisher makeRequestBody(Object req) {
        if (req != null) {
            return HttpRequest.BodyPublishers.ofString(new Gson().toJson(req));
        }
        else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest req) throws Exception {
        try {
            return client.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }



    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws Exception {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null && !body.isEmpty()) {
                var error = new Gson().fromJson(body, Exception.class);
                throw new Exception(error);
            }

            throw new Exception("Unknown server error, status: " + status);
        }

        if (responseClass == null) {
            return null;
        }

        var body = response.body();
        if (body == null || body.isEmpty()) {
            return null;
        }

        return new Gson().fromJson(body, responseClass);
    }



    private boolean isSuccessful(int status) { return status / 100 == 2;}


    public void joinGame(JoinGameRequest data, String authToken) throws Exception {
        var json = new Gson().toJson(data);

        var req = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game"))
                .header("authorization", authToken)
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        var res = sendRequest(req);

        if (!isSuccessful(res.statusCode())) {
            throw new Exception("Login failed: " + res.body());
        }
    }
}
