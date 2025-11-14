import chess.*;
import ui.EscapeSequences;

import java.awt.color.ICC_ColorSpace;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        ChessGame board = new ChessGame();
        System.out.print("♕ 240 Chess Client: type help to get started.\n");
        Scanner scanner = new Scanner(System.in);
        boolean loggedIn = false;


        while (true) {
            if (loggedIn) {
                System.out.print("[LOGGED_IN] >>> ");
            } else {
                System.out.print("[LOGGED_OUT] >>> ");
            }
            var line = scanner.nextLine();
//            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
//            System.out.print(line + EscapeSequences.BLACK_BISHOP);
//            System.out.print((EscapeSequences.SET_BG_COLOR_DARK_GREEN));
//            System.out.print(line + EscapeSequences.WHITE_BISHOP);
            //main.drawChessBoard(board, ChessGame.TeamColor.WHITE);
            String[] inputs = line.split(" ");

            if (loggedIn) {
                if (inputs[0].equalsIgnoreCase("help")) {
                    helpLoggedIn();
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
                    //
                }
                else if (inputs[0].equalsIgnoreCase("register")) {
                    break;
                }
                else {
                    System.out.println("Bad input, you are logged out, type help to see what you can do!");
                }
            }
        }
    }


    static void helpLoggedIn() {
        System.out.println("""
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
        login <USERNAME> <PASSWORD>            - to play chess
        quit                                   - playing chess
        help                                   - with possible commands
        """);
    }


    private void printLetterRow(ChessGame.TeamColor color) {
        String letters = color == ChessGame.TeamColor.WHITE ? "    a   b   c  d   e   f  g   h    " : "    h   g   f  e   d   c  b   a    ";
        System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.print(letters);
        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        System.out.print("\n");
    }

    private void printNumber(int row) {
        System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.print(" " + row + " ");
        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }



    private void drawChessBoard(ChessGame board, ChessGame.TeamColor color) {

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


                if ((row + col) % 2 == 0) {
                    System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
                } else {
                    System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
                }

                if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    var piece = currentBoard.getPiece(new ChessPosition(row, col));
                    if (piece == null) {
                        //System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
                        System.out.print(EscapeSequences.EMPTY);
                        System.out.print(EscapeSequences.RESET_BG_COLOR);
                    }
                    else {
                        var pieceColor = piece.getTeamColor();
                        String s = pieceColor == ChessGame.TeamColor.WHITE ? EscapeSequences.SET_TEXT_COLOR_BLUE : EscapeSequences.SET_TEXT_COLOR_RED;
                        System.out.print(s);
                        switch (piece.getPieceType()) {
                            case ROOK -> System.out.print(
                                    pieceColor == ChessGame.TeamColor.WHITE ?
                                            EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK);
                            case BISHOP -> System.out.print(
                                    pieceColor == ChessGame.TeamColor.WHITE ?
                                            EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP);
                            case KNIGHT -> System.out.print(
                                    pieceColor == ChessGame.TeamColor.WHITE ?
                                            EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT);
                            case KING -> System.out.print(
                                    pieceColor == ChessGame.TeamColor.WHITE ?
                                            EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING);
                            case QUEEN -> System.out.print(
                                    pieceColor == ChessGame.TeamColor.WHITE ?
                                            EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN);
                            case PAWN -> System.out.print(
                                    pieceColor == ChessGame.TeamColor.WHITE ?
                                            EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN);
                        }
                        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                        System.out.print(EscapeSequences.RESET_BG_COLOR);
                    }
                }
            }
            printNumber(row);
            System.out.print("\n");
        }

        printLetterRow(color);
    }
}