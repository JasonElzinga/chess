package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

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
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = new Gson().fromJson(ctx.message(), MakeMoveCommand.class);
                    makeMove(moveCommand, ctx.session);
                }
                case LEAVE -> leave(command, ctx.session);
                case RESIGN -> resign(command, ctx.session);
            }
        } catch (IOException | DataAccessException ex) {
            ex.printStackTrace();
        }
    }

    private void resign(UserGameCommand command, Session session) throws IOException {
        try {
            var username = dataAccess.getAuthData(command.getAuthToken()).username();
            var gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                throw new DataAccessException("Game is null");
            }
            var game = gameData.game();
            var state = game.getBoardState();

            if (state == ChessGame.State.FINISHED) {
                throw new DataAccessException("Game is already finished");
            }

            ChessGame.TeamColor playingColor;
            String whiteUsername;
            String blackUsername;
            boolean observer = !(username.equalsIgnoreCase(gameData.whiteUsername()) ||
                    username.equalsIgnoreCase(gameData.blackUsername()));

            if (gameData.whiteUsername() != null) {
                if (gameData.whiteUsername().equalsIgnoreCase(username)) {
                    playingColor = ChessGame.TeamColor.WHITE;
                } else {
                    playingColor = ChessGame.TeamColor.BLACK;
                }
            } else if (gameData.blackUsername() != null) {
                if (gameData.blackUsername().equalsIgnoreCase(username)) {
                    playingColor = ChessGame.TeamColor.BLACK;
                } else {
                    playingColor = ChessGame.TeamColor.WHITE;
                }
            }
            if (observer) {
                throw new DataAccessException("You are an observer you cannot resign");
            }
            var msg = username + " has left ";
            var notification = new NotificationMessage(msg);

            connections.broadcast(session, notification, gameData.gameID());
            connections.loadGame(session, notification);
            //connections.remove(session, command.getGameID());
            game.setBoardState();
            dataAccess.updateGame(game, gameData.gameID());

        } catch (Exception e) {
            var errorMessage = new ErrorMessage("Error: " + e.getMessage());
            connections.errorMessage(session, errorMessage);
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void makeMove(MakeMoveCommand command, Session session) throws IOException {
        ChessGame newGame;
        try {
            var username = dataAccess.getAuthData(command.getAuthToken()).username();
            var gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                throw new DataAccessException("Game is null");
            }
            var game = gameData.game();
            var state = game.getBoardState();

            if (state == ChessGame.State.FINISHED) {
                throw new DataAccessException("Game is already finished");
            }

            var teamTurn = game.getTeamTurn();
            ChessGame.TeamColor playingColor;

            if (gameData.whiteUsername().equalsIgnoreCase(username)) {
                playingColor = ChessGame.TeamColor.WHITE;
            } else {
                playingColor = ChessGame.TeamColor.BLACK;
            }
            if (playingColor != teamTurn) {
                throw new DataAccessException("Not your turn");
            }

            ChessMove move = command.getChessMove();
            if (move == null) {
                throw new DataAccessException("Move is null");
            }
            try {
                gameData.game().makeMove(move);
                gameData.game().setTeamTurn(playingColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE);
                dataAccess.updateGame(gameData.game(), gameData.gameID());
                newGame = dataAccess.getGame(gameData.gameID()).game();
                if (newGame.equals(gameData.game())) {
                    System.out.println("The move didn't actually work");
                }
            } catch (Exception e) {
                throw new DataAccessException("Move was invalid");
            }

            var gameJson = new Gson().toJson(newGame);
            var makeMoveMessage = new LoadGameMessage(gameJson);
            connections.makeMove(gameData.gameID(), makeMoveMessage);
            var notification = new NotificationMessage("move: " + move);
            connections.broadcast(session, notification, gameData.gameID());
        } catch (Exception e) {
            var errorMessage = new ErrorMessage("Error: " + e.getMessage());
            connections.errorMessage(session, errorMessage);
        }
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
            var errorMessage = new ErrorMessage("Error: " + e.getMessage());
            connections.errorMessage(session, errorMessage);
        }

    }

    private void leave(UserGameCommand command, Session session) throws IOException, DataAccessException {
        try {
            var username = dataAccess.getAuthData(command.getAuthToken()).username();
            var gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                throw new DataAccessException("Game is null");
            }
            var game = gameData.game();
            ChessGame.TeamColor playingColor;
            if (gameData.whiteUsername() != null) {
                if (gameData.whiteUsername().equalsIgnoreCase(username)) {
                    playingColor = ChessGame.TeamColor.WHITE;
                } else {
                    playingColor = ChessGame.TeamColor.BLACK;
                }
            } else {
                if (gameData.blackUsername().equalsIgnoreCase(username)) {
                    playingColor = ChessGame.TeamColor.BLACK;
                } else {
                    playingColor = ChessGame.TeamColor.WHITE;
                }
            }
            var msg = username + " has left ";
            var notification = new NotificationMessage(msg);

            connections.broadcast(session, notification, gameData.gameID());
            connections.remove(session, command.getGameID());
            dataAccess.updateUserGame(game, command.getGameID(), playingColor);

        } catch (Exception e) {
            var errorMessage = new ErrorMessage("Error: " + e.getMessage());
            connections.errorMessage(session, errorMessage);
        }
    }

}