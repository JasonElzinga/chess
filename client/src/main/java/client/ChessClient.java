package client;

import chess.*;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import com.google.gson.Gson;
import exception.ResponseException;
import model.CreateGameRequest;
import model.JoinGameRequest;
import model.ListGameResponse;
import model.UserData;
import ui.EscapeSequences;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ChessClient implements NotificationHandler {

    private final ServerFacade facade;
    private final WebSocketFacade ws;
    private boolean playing;
    private String username;
    private boolean loggedIn;
    private boolean observing;
    private ChessGame.TeamColor playingColor;
    private ChessGame game;
    private int gameID;
    private String authToken;

    public ChessClient(String serverUrl) throws ResponseException {
        facade = new ServerFacade(serverUrl);
        ws = new WebSocketFacade(serverUrl, this);
    }


    public void run(String[] args) throws ResponseException {
        //var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

        System.out.print("♕ 240 Chess Client: type help to get started.\n");
        Scanner scanner = new Scanner(System.in);
        int[][] currentGames = new int[1][1];
        currentGames[0][0] = 0;

        this.loggedIn = false;
        this.username = "";
        this.playing = false;
        this.observing = false;
        this.playingColor = ChessGame.TeamColor.WHITE;
        this.authToken = "";

        while (true) {
            padding(loggedIn, playing, observing, username, playingColor);

            var line = scanner.nextLine();
            String[] inputs = line.split(" ");


            if (playing || observing) {
                if (inputs[0].equalsIgnoreCase("help")) {
                    helpPlaying();
                }
                else if (inputs[0].equalsIgnoreCase("redraw")) {
                    drawChessBoard(game, playingColor, null, null);
                }
                else if (inputs[0].equalsIgnoreCase("leave")) {
                    ws.connect(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
                    observing = false;
                    playing = false;
                }
                else if (inputs[0].equalsIgnoreCase("move")) {
                    ChessPiece.PieceType pieceType;
                    if (inputs.length == 2) {
                        pieceType = null;
                    } else if (inputs.length == 3) {
                        pieceType = getPieceType(inputs[2]);
                        if (pieceType == null) {
                            wrongInputs();
                            continue;
                        }
                    } else {
                        wrongInputs();
                        continue;
                    }
                    ChessMove move;
                    try {
                        move = getChessMove(inputs[1], pieceType);
                    } catch (Exception e) {
                        wrongInputs();
                        continue;
                    }
                    ws.connect(new MakeMoveCommand(authToken, gameID, move));
                }
                else if (inputs[0].equalsIgnoreCase("resign")) {
                    if (confirmResignation()) {
                        ws.connect(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
                    }
                } else if (inputs[0].equalsIgnoreCase("display")) {
                    if (inputs.length != 2) {
                        wrongInputs();
                        continue;
                    }
                    String inputtedPos = inputs[1];
                    if (inputtedPos.length() != 2) {
                        wrongInputs();
                        continue;
                    }
                    ChessPosition pos;
                    try {
                        pos = getPosition(inputs[1]);
                    } catch (Exception e) {
                        wrongInputs();
                        continue;
                    }
                    displayValidMoves(pos);
                }
                else {
                    wrongInputs();
                    continue;
                }
            }
            // TODO you need to fix so it will get rid of the player when you leave
            // TODO when you just exit without leaving what happens
            // TODO else statement saying bad input
            // TODO notifications for check and checkmate
            // TODO check for check and checkmate before giving the valid moves


            else if (loggedIn) {
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
                    String colorStr;
                    if (inputs[2].equalsIgnoreCase("white")) {
                        color = ChessGame.TeamColor.WHITE;
                        colorStr = "white";
                    }
                    else if (inputs[2].equalsIgnoreCase("black")){
                        color = ChessGame.TeamColor.BLACK;
                        colorStr = "black";
                    }
                    else {
                        System.out.println("joining game failed, you didn't provide the right color to join the game");
                        continue;
                    }
                    try {
                        intendedGameID = currentGames[intendedGameID - 1][0];
                    } catch (Exception e) {
                        wrongInputs();
                    }
                    String msg;
                    try {
                        facade.joinGame(new JoinGameRequest(colorStr, intendedGameID), authToken);
                        playing = true;
                        playingColor = color;
                        //game = new ChessGame();

                        ws.connect(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, intendedGameID));
                        gameID = intendedGameID;
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
                    try {
                        intendedGameID = currentGames[intendedGameID - 1][0];
                    } catch (Exception e) {
                        wrongInputs();
                    }
                    if (intendedGameID <= 0) {
                        wrongInputs();
                        continue;
                    }
                    try {
                        ws.connect(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, intendedGameID));
                        observing = true;
                        gameID = intendedGameID;
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

    private ChessPosition getPosition(String pos) throws Exception {
        String[] letters = {
                "a",
                "b",
                "c",
                "d",
                "e",
                "f",
                "g",
                "h"
        };
        String test1 = pos.substring(0,0);
        for (var l : letters) {
            if (test1.equalsIgnoreCase(l)) {
                throw new Exception("This is not a letter");
            }
        }

        int posCol = pos.charAt(0) - 'a' + 1;
        int posRow = pos.charAt(1) - '0';

        if ((posCol < 1 || posCol > 8) && (posRow < 1 || posRow > 8)) {
            throw new Exception("Bad input");
        }
        return new ChessPosition(posRow, posCol);
    }

    private void displayValidMoves(ChessPosition startPos) {
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE) || game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            drawChessBoard(game, playingColor, null, null);
        }
        var validMoves = game.validMoves(startPos);
        Set<ChessPosition> chessPositionsToHighlight = new HashSet<>();
        for (var m : validMoves) {
            ChessPosition pos = m.getEndPosition();
            chessPositionsToHighlight.add(pos);
        }

        drawChessBoard(game, playingColor, chessPositionsToHighlight, startPos);
    }

    private ChessPiece.PieceType getPieceType(String type) {
        if (type.equalsIgnoreCase("Rook")) {
            return ChessPiece.PieceType.ROOK;
        }
        if (type.equalsIgnoreCase("Knight")) {
            return ChessPiece.PieceType.KNIGHT;
        }
        if (type.equalsIgnoreCase("Bishop")) {
            return ChessPiece.PieceType.BISHOP;
        }
        if (type.equalsIgnoreCase("Queen")) {
            return ChessPiece.PieceType.QUEEN;
        }
        return null;
    }

    private ChessMove getChessMove(String move,  ChessPiece.PieceType promotionType) throws Exception {

        String[] letters = {
                "a",
                "b",
                "c",
                "d",
                "e",
                "f",
                "g",
                "h"
        };

        String test1 = move.substring(0,0);
        for (var l : letters) {
            if (test1.equalsIgnoreCase(l)) {
                throw new Exception("This is not a letter");
            }
        }

        String test2 = move.substring(2,2);
        for (var l : letters) {
            if (test2.equalsIgnoreCase(l)) {
                throw new Exception("This is not a letter");
            }
        }


        int fromCol = move.charAt(0) - 'a' + 1;
        int fromRow = move.charAt(1) - '0';

        int toCol = move.charAt(2) - 'a' + 1;
        int toRow = move.charAt(3) - '0';

        return new ChessMove(new ChessPosition(fromRow, fromCol), new ChessPosition(toRow, toCol), promotionType);
    }

    private boolean confirmResignation() {
        System.out.print("Are you sure you want to resign? (y or n) >>> ");

        Scanner scanner = new Scanner(System.in);
        var line = scanner.nextLine();
        String[] inputs = line.split(" ");

        if (inputs[0].equalsIgnoreCase("y")) {
            return true;
        }
        return false;
    }


    private void padding(boolean loggedIn, boolean playing, boolean observing, String username, ChessGame.TeamColor playingColor) {
        if (loggedIn && (!playing && !observing)) {
            System.out.print("[LOGGED_IN] >>> ");
        } else if (playing) {
            System.out.print("[PLAYING as " + username + " as " + playingColor + "] >>> ");
        } else if (observing) {
            System.out.print("[Observing] >>> ");
        }
        else {
            System.out.print("[LOGGED_OUT] >>> ");
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

    static void helpPlaying() {
        System.out.print("""
        redraw               - chess board
        leave                - game
        move <starting location><end location> (e2e4) <promotion piece> (if pawn will be promoted>)
        resign               - game
        help                 - with possible commands
        display <position> (e2) - to get the legal moves of that piece as shown by highlighted squares
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



    static private void drawChessBoard(ChessGame board, ChessGame.TeamColor color, Set<ChessPosition> chessPositionsToHighlight, ChessPosition startPos) {

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
                if (chessPositionsToHighlight == null) {
                    printSquare(currentBoard, row, col);
                } else {
                    printHighlightedSquare(currentBoard, row, col, chessPositionsToHighlight, startPos);
                }
            }
            printNumber(row);
            System.out.print("\n");
        }
        printLetterRow(color);
    }


    private static void printHighlightedSquare(ChessBoard currentBoard, int row, int col, Set<ChessPosition> chessPositionsToHighlight, ChessPosition startPos) {
        // Square color
        boolean highlighted = false;
        if (startPos.getRow() == row && startPos.getColumn() == col) {
            System.out.print(EscapeSequences.SET_BG_COLOR_YELLOW);
            highlighted = true;
        }
        else {
            for (var pos : chessPositionsToHighlight) {
                if (pos.getRow() == row && pos.getColumn() == col) {
                    System.out.print(EscapeSequences.SET_BG_COLOR_GREEN);
                    highlighted = true;
                }
            }
        }
        if (!highlighted &&((row + col) % 2 == 0)) {
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
        } else if (!highlighted &&((row+col) % 2 != 0)) {
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


    @Override
    public void notify(ServerMessage notification) {
        switch (notification.getServerMessageType()) {
            case NOTIFICATION -> {
                System.out.println(notification.getMessage());
                padding(loggedIn, playing, observing, username, playingColor);

            }
            case ERROR -> {
                System.out.println(notification.getMessage());
                padding(loggedIn, playing, observing, username, playingColor);
            }
            case LOAD_GAME -> loadGame((LoadGameMessage) notification);
        }

    }

    private void loadGame(LoadGameMessage notification) {
         game = new Gson().fromJson(notification.getMessage(), ChessGame.class);
         System.out.println();
         drawChessBoard(game, playingColor, null, null);
         padding(loggedIn, playing, observing, username, playingColor);
    }
}