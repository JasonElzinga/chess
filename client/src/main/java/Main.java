import chess.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: type help to get started.");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            var line = scanner.nextLine();
            System.out.println(line);
        }
    }


    private void help() {
        System.out.println("register <USERNAME> <PASSWORD>");
    }
}