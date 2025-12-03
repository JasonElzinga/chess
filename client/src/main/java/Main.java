import chess.*;
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

    public static void main(String[] args) throws ResponseException {
        //var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        Main handler = new Main();
        var facade = new ServerFacade("http://localhost:8080");
        WebSocketFacade ws = new WebSocketFacade("http://localhost:8080");
        ChessGame board = new ChessGame();
        System.out.print("♕ 240 Chess Client: type help to get started.\n");
        Scanner scanner = new Scanner(System.in);
        boolean loggedIn = false;
        String authToken = "";
        String username = "";
        int[][] currentGames = new int[1][1];
        currentGames[0][0] = 0;
        boolean playing = false;
        ChessGame.TeamColor playingColor = ChessGame.TeamColor.WHITE;


        while (true) {
            if (loggedIn && !playing) {
                System.out.print("[LOGGED_IN] >>> ");
            } else if (playing) {
                System.out.print("[PLAYING] " + username + playingColor);
            }
            else {
                System.out.print("[LOGGED_OUT] >>> ");
            }
            var line = scanner.nextLine();
            String[] inputs = line.split(" ");

            if (loggedIn) {
                if (inputs[0].equalsIgnoreCase("help")) {
                    helpLoggedIn();
                }
                else if (inputs[0].equalsIgnoreCase("logout")) {
                    try {
                        facade.logout(authToken);
                        loggedIn = false;
                        authToken = "";
                        username = "";
                        System.out.println("Logged out");
                    } catch (Exception e) {
                        error("logging out failed");
                    }
                }
                else if (inputs[0].equalsIgnoreCase("list")) {
                    if (inputs.length != 1) {
                        wrongInputs();
                        continue;
                    }
                    try {
                        var games = facade.listGames(authToken);
                        //System.out.println(games);
                        listGames(games);
                        currentGames = assignNumbersToGames(games);
                    } catch (Exception e) {
                        error("listing games failed");
                    }
                }
                else if (inputs[0].equalsIgnoreCase("join")) {
                    if (inputs.length != 3) {
                        wrongInputs();
                        continue;
                    }
                    if (currentGames[0][0] == 0) {
                        System.out.println("joining game failed, you need to list the games first and use those numbers to join");
                        continue;
                    }
                    int intendedGameID;
                    try {
                        intendedGameID = Integer.parseInt(inputs[1]);
                    } catch (Exception e) {
                        System.out.println("joining game failed, you didn't provide a number to join the game");
                        continue;
                    }
                    if (intendedGameID <=0) {
                        wrongInputs();
                        continue;
                    }
                    ChessGame.TeamColor color;
                    if (inputs[2].equalsIgnoreCase("WHITE")) {
                        color = ChessGame.TeamColor.WHITE;
                    }
                    else if (inputs[2].equalsIgnoreCase("BLACK")){
                        color = ChessGame.TeamColor.BLACK;
                    }
                    else {
                        System.out.println("joining game failed, you didn't provide the right color to join the game");
                        continue;
                    };
                    intendedGameID = currentGames[intendedGameID-1][0];
                    try {
                        facade.joinGame(new JoinGameRequest(inputs[2], intendedGameID), authToken);
                        board = new ChessGame();
                        drawChessBoard(board,color);
                        ws.connect(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, intendedGameID));
                    } catch (Exception e) {
                        error("joining game failed");
                    }
                }
                else if (inputs[0].equalsIgnoreCase("create")) {
                    if (inputs.length != 2) {
                        wrongInputs();
                        continue;
                    }
                    try {
                        var res = facade.createGame(new CreateGameRequest(inputs[1]), authToken);
                        System.out.println("Created game: " + inputs[1]);

                    } catch (Exception e) {
                        error("Failed creating the game");
                    }
                }
                else if (inputs[0].equalsIgnoreCase("observe")) {
                    if (inputs.length != 2) {
                        wrongInputs();
                        continue;
                    }
                    if (currentGames[0][0] == 0) {
                        System.out.println("joining game failed, you need to list the games first and use those numbers to join");
                        continue; //this is important
                    }
                    int intendedGameID = 0; //getID
                    try {
                        intendedGameID = Integer.parseInt(inputs[1]);
                    } catch (Exception e) {
                        System.out.println("joining game failed, you didn't provide a number to join the game");
                        continue;
                    }
                    if (intendedGameID > inputs.length +1 || intendedGameID <= 0) {
                        wrongInputs();
                        continue;
                    } // -4 for joining, make sure to print error to join game 27, and then observe
                    try {
                        board = new ChessGame();
                        drawChessBoard(board, ChessGame.TeamColor.WHITE);

                    } catch (Exception e) {
                        error("Failed observing the game");
                    }
                }
                else {
                    wrongInputs();
                }
            }
            else {
                if (inputs[0].equalsIgnoreCase("help")) {
                    helpLoggedOut();
                }
                else if (inputs[0].equalsIgnoreCase("quit")) {
                    break;
                }
                else if (inputs[0].equalsIgnoreCase("login")) {
                    if (inputs.length != 4) {
                        wrongInputs();
                        continue;
                    }

                    try {
                        var res = facade.login(new UserData(inputs[1], inputs[2], inputs[3]));
                        loggedIn = true;
                        authToken = res.authToken();
                        username = res.username();
                        System.out.println("Logged in as: " + username);

                    } catch (Exception e) {
                        error("Failed logging in");
                    }
                }
                else if (inputs[0].equalsIgnoreCase("clear")) {
                    try {
                        facade.clear();
                    } catch (Exception e) {
                        error("clearing the database failed");
                    }
                }
                else if (inputs[0].equalsIgnoreCase("register")) {
                    if (inputs.length != 4) {
                        wrongInputs();
                        continue;
                    }
                    username = inputs[1];
                    var password = inputs[2];
                    var email = inputs[3];
                    try {
                        var res = facade.register(new UserData(username, password, email));
                        System.out.println("registered as: " + res.username());
                        loggedIn = true;
                        authToken = res.authToken();
                    } catch (Exception e) {
                        error("registering failed");
                    }
                }
                else {
                    wrongInputs();
                }
            }
        }
    }

    static int[][] assignNumbersToGames(ListGameResponse res) {
        var games = res.games();
        int[][] arr = new int[games.size()][2];

        int assigned = 1;
        int index = 0;

        for (var g : games) {
            arr[index][0] = assigned;
            arr[index][1] = g.gameID();
            assigned++;
            index++;
        }

        return arr;
    }

    static void listGames(ListGameResponse games) {
        int i = 0;
        for (var game : games.games()) {
            i++;
            System.out.printf("Game %d: Name: %s (White %s, Black: %s)%n",
                    i,
                    game.gameName(),
                    game.whiteUsername(),
                    game.blackUsername());
        }
    }

    static void wrongInputs() {
        System.out.println("Bad input, type help to see what you can do!");
    }

    static void error(String errorMessage) {
        System.out.println(errorMessage);
    }

    static void helpLoggedIn() {
        System.out.print("""
        create <NAME>        - a game
        list                 - games
        join <ID> [WHITE|BLACK] - a game
        observe <ID>         - a game
        logout               - when you are done
        quit                 - playing chess
        help                 - with possible commands
        """);
    }

    static void helpLoggedOut() {
        System.out.print("""
        register <USERNAME> <PASSWORD> <EMAIL> - to create an account
        login <USERNAME> <PASSWORD> <EMAIL>    - to play chess
        quit                                   - playing chess
        help                                   - with possible commands
        """);
    }


    private static void printLetterRow(ChessGame.TeamColor color) {
        String letters = color == ChessGame.TeamColor.WHITE ? "    a   b   c  d   e   f  g   h    " : "    h   g   f  e   d   c  b   a    ";
        System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.print(letters);
        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        System.out.print("\n");
    }

    private static void printNumber(int row) {
        System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.print(" " + row + " ");
        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }



    static private void drawChessBoard(ChessGame board, ChessGame.TeamColor color) {

        var currentBoard = board.getBoard();
        boolean blackPerspective = (color == ChessGame.TeamColor.BLACK);

        int rowStart = blackPerspective ? 1 : 8;
        int rowStep = blackPerspective ? 1 : -1;
        int rowEnd = blackPerspective ? 8 : 1;

        int colStart = blackPerspective ? 8 : 1;
        int colStep = blackPerspective ? -1 : 1;
        int colEnd = blackPerspective ? 1 : 8;

        printLetterRow(color);
        for (int row = rowStart; row != rowEnd + rowStep; row +=rowStep) {
            printNumber(row);
            for (int col = colStart; col != colEnd + colStep; col += colStep) {
                printSquare(currentBoard, row, col);
            }
            printNumber(row);
            System.out.print("\n");
        }
        printLetterRow(color);
    }

    private static void printSquare(ChessBoard currentBoard, int row, int col) {
        // Square color
        if ((row + col) % 2 == 0) {
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
        } else {
            System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
        }

        var piece = currentBoard.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            System.out.print(EscapeSequences.EMPTY);
            System.out.print(EscapeSequences.RESET_BG_COLOR);
            return;
        }

        var pieceColor = piece.getTeamColor();
        String s = pieceColor == ChessGame.TeamColor.WHITE ?
                EscapeSequences.SET_TEXT_COLOR_BLUE : EscapeSequences.SET_TEXT_COLOR_RED;
        System.out.print(s);

        switch (piece.getPieceType()) {
            case ROOK -> System.out.print(pieceColor == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK);
            case BISHOP -> System.out.print(pieceColor == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP);
            case KNIGHT -> System.out.print(pieceColor == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT);
            case KING -> System.out.print(pieceColor == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING);
            case QUEEN -> System.out.print(pieceColor == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN);
            case PAWN -> System.out.print(pieceColor == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN);
        }

        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        System.out.print(EscapeSequences.RESET_BG_COLOR);
    }


}