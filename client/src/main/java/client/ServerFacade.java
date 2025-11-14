package client;

import com.google.gson.Gson;
import model.RegisterResponse;
import model.UserData;

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
        var req = buildRequest("POST", "/user", data);
        var res = sendRequest(req);
        return handleResponse(res, RegisterResponse.class);
    }

    public void clear() throws Exception {
        var req = buildRequest("DELETE", "/db", null);
        sendRequest(req);
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
                throw new Exception(error.getMessage());
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


}
