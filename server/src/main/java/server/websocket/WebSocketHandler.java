package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exception.ResponseException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.Session;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final DataAccess dataAccess;

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    private final ConnectionManager connections = new ConnectionManager();

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command, ctx.session);
//                case MAKE_MOVE -> exit(command.visitorName(), ctx.session);
//                case LEAVE -> exit(command.visitorName(), ctx.session);
//                case RESIGN -> exit(command.visitorName(), ctx.session);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void enter(String visitorName, Session session) throws IOException {
//        connections.add(session);
//        var message = String.format("%s is in the shop", visitorName);
//        var notification = new Notification(Notification.Type.ARRIVAL, message);
//        connections.broadcast(session, notification);
    }

    private void connect(UserGameCommand command, Session session) throws IOException {
        try {
            var username = dataAccess.getAuthData(command.getAuthToken()).username();
            var game = dataAccess.getGame(command.getGameID());
            if (game == null) {
                throw new DataAccessException("Game is null");
            }
            ChessGame.TeamColor playingColor;
            if (game.whiteUsername().equalsIgnoreCase(username)) {
                playingColor = ChessGame.TeamColor.WHITE;
            } else {
                playingColor = ChessGame.TeamColor.BLACK;
            }
            var msg = username + " is playing " + playingColor;
            //System.out.println(msg);
            var notification = new NotificationMessage(msg);

            connections.broadcast(session, notification, command.getGameID());

            var serializer = new Gson();
            var gameJson = serializer.toJson(game.game());
            var loadGameNotification = new LoadGameMessage(gameJson);
            connections.loadGame(session, loadGameNotification);
            connections.add(session, command.getGameID());
        } catch (Exception e) {
            var errorMessage = new ErrorMessage("Error loading the game didn't work");
            connections.errorMessage(session, errorMessage);
        }

    }

    private void exit(String visitorName, Session session) throws IOException {
//        var message = String.format("%s left the shop", visitorName);
//        var notification = new Notification(Notification.Type.DEPARTURE, message);
//        connections.broadcast(session, notification);
//        connections.remove(session);
    }

    public void makeNoise(String petName, String sound) throws ResponseException {
//        try {
//            var message = String.format("%s says %s", petName, sound);
//            var notification = new Notification(Notification.Type.NOISE, message);
//            connections.broadcast(null, notification);
//        } catch (Exception ex) {
//            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
//        }
    }
}