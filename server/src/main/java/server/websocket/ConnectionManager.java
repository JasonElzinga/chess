package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;
//import webSocketMessages.Notification;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Session, Session> connections = new ConcurrentHashMap<>();

    public void add(Session session, int gameID) {
        gameSessions.computeIfAbsent(gameID, id -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void remove(Session session, int gameID) {
        connections.remove(session);
    }

    public void broadcast(Session excludeSession, ServerMessage notification, int gameID) throws IOException {
        Set<Session> sessions = gameSessions.get(gameID);
        var serializer = new Gson();

        if (sessions != null) {
            for (Session s : sessions) {
                if (s.isOpen()) {
                    s.getRemote().sendString(serializer.toJson(notification));
                }
            }
        }
    }

    public void loadGame(Session session, ServerMessage notification) throws IOException {

        var serializer = new Gson();

        session.getRemote().sendString(serializer.toJson(notification));
//        var serializer = new Gson();
//
//        for (Session c : connections.values()) {
//            if (c.equals(excludeSession)) {
//                c.getRemote().sendString(serializer.toJson(notification));
//            }
//        }
    }

    public void errorMessage(Session session, ServerMessage notification) throws IOException {

        var serializer = new Gson();

        session.getRemote().sendString(serializer.toJson(notification));

//        for (Session c : connections.values()) {
//            if (c.isOpen()) {
//                if (c.equals(session)) {
//                    c.getRemote().sendString(serializer.toJson(notification));
//                }
//            }
//        }
    }
}
