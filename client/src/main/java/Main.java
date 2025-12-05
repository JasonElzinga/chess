import chess.*;
import client.ChessClient;
import client.ServerFacade;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import exception.ResponseException;
import model.CreateGameRequest;
import model.JoinGameRequest;
import model.ListGameResponse;
import model.UserData;
import ui.EscapeSequences;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.management.NotificationFilter;
import java.awt.color.ICC_ColorSpace;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        try {
            new ChessClient(serverUrl).run(args);

        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
    }

}