package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;
//import webSocketMessages.Notification;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Session, Session> connections = new ConcurrentHashMap<>();

    public void add(Session session) {
        connections.put(session, session);
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public void broadcast(Session excludeSession, ServerMessage notification) throws IOException {

        var serializer = new Gson();

        for (Session c : connections.values()) {
            if (c.isOpen()) {
                if (!c.equals(excludeSession)) {
                    c.getRemote().sendString(serializer.toJson(notification));
                }
            }
        }
    }

    public void loadGame(Session excludeSession, ServerMessage notification) throws IOException {

        var serializer = new Gson();

        for (Session c : connections.values()) {
            if (c.equals(excludeSession)) {
                c.getRemote().sendString(serializer.toJson(notification));
            }
        }
    }

    public void errorMessage(Session excludeSession, ServerMessage notification) throws IOException {

        var serializer = new Gson();

        for (Session c : connections.values()) {
            if (c.isOpen()) {
                if (c.equals(excludeSession)) {
                    c.getRemote().sendString(serializer.toJson(notification));
                }
            }
        }
    }
}
