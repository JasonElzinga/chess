package server.websocket;

import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
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
        var sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(session);

            if (sessions.isEmpty()) {
                gameSessions.remove(gameID);
            }
        }
    }

    public void broadcast(Session excludeSession, ServerMessage notification, Integer gameID) throws IOException {
        Set<Session> sessions = gameSessions.get(gameID);
        var serializer = new Gson();

        if (sessions != null) {
            for (Session s : sessions) {
                if (s.isOpen()) {
                    if (s!= excludeSession) {
                        s.getRemote().sendString(serializer.toJson(notification));
                    }
                }
            }
        }
    }

    public void loadGame(Session session, ServerMessage notification) throws IOException {

        var serializer = new Gson();

        session.getRemote().sendString(serializer.toJson(notification));
    }

    public void errorMessage(Session session, ServerMessage notification) throws IOException {

        var serializer = new Gson();

        session.getRemote().sendString(serializer.toJson(notification));

    }

    public void makeMove(int gameID, LoadGameMessage notification) throws IOException {
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

    public void specialState(Session session, NotificationMessage notification) throws IOException {

        var serializer = new Gson();
        session.getRemote().sendString(serializer.toJson(notification));
    }
}
