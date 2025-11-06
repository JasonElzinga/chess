import chess.*;
import ui.EscapeSequences;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        ChessGame board = new ChessGame();
        System.out.println("♕ 240 Chess Client: type help to get started.");
        Scanner scanner = new Scanner(System.in);
        var main = new Main();
        while (true) {
            var line = scanner.nextLine();
//            System.out.println(EscapeSequences.SET_TEXT_COLOR_BLACK);
//            System.out.println(line + EscapeSequences.BLACK_BISHOP);
//            System.out.println((EscapeSequences.SET_BG_COLOR_DARK_GREEN));
//            System.out.println(line + EscapeSequences.WHITE_BISHOP);
            main.drawChessBoard(board, ChessGame.TeamColor.WHITE);
        }
    }


    private void help() {
        System.out.println("register <USERNAME> <PASSWORD>");
    }


    private void drawChessBoard(ChessGame board, ChessGame.TeamColor color) {
//        String[] letters = {
//                "a",
//                "b",
//                "c",
//                "d",
//                "e",
//                "f",
//                "g",
//                "h"
//        };
//
//        String[] blackBackRow = {
//                EscapeSequences.BLACK_ROOK,
//                EscapeSequences.BLACK_KNIGHT,
//                EscapeSequences.BLACK_BISHOP,
//                EscapeSequences.BLACK_QUEEN,
//                EscapeSequences.BLACK_KING,
//                EscapeSequences.BLACK_BISHOP,
//                EscapeSequences.BLACK_KNIGHT,
//                EscapeSequences.BLACK_ROOK,
//        };
//
//        String[] whiteBackRow = {
//                EscapeSequences.WHITE_ROOK,
//                EscapeSequences.WHITE_KNIGHT,
//                EscapeSequences.WHITE_BISHOP,
//                EscapeSequences.WHITE_QUEEN,
//                EscapeSequences.WHITE_KING,
//                EscapeSequences.WHITE_BISHOP,
//                EscapeSequences.WHITE_KNIGHT,
//                EscapeSequences.WHITE_ROOK,
//        };

        var currentBoard = board.getBoard();
        boolean whitePerspective = (color == ChessGame.TeamColor.WHITE);

        int rowStart = whitePerspective ? 1 : 8;
        int rowStep = whitePerspective ? 1 : -1;
        int rowEnd = whitePerspective ? 8 : 1;


        for (int row = rowStart; row != rowEnd; row +=rowStep) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_DARK_GREY);
            for (int col = rowStart; col != rowEnd; col +=rowStep) {
                var piece = currentBoard.getPiece(new ChessPosition(row, col));
                if (piece == null) {
                    //
                }
                else {
                    switch (piece.getPieceType()) {
                        case ROOK -> System.out.println(
                                piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                                        EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK);
                    }
                }
            }
        }
    }
}