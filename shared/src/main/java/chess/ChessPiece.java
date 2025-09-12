package chess;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.abs;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.BISHOP) {
            return bishop_movement(board, myPosition);
        }
        if (piece.getPieceType() == PieceType.KING) {
            return king_movement(board, myPosition);
        }
        return List.of();
    }


    private Collection<ChessMove> king_movement(ChessBoard board, ChessPosition myPosition) {
        // Check moving up
        int x = myPosition.getRow();
        int y = myPosition.getColumn();
        Collection<ChessMove> lst = new ArrayList<ChessMove>();
        int[][] possible_moves = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
        for (int[] possible_move : possible_moves) {
            int new_row = x + possible_move[0];
            int new_column = y + possible_move[1];
            if (new_row>=0 && new_row<=8 && new_column >= 0 && new_column <=8) {
                lst.add(new ChessMove(myPosition, new ChessPosition(new_row, new_column), null));
            }
        }
        return lst;
    }



    private Collection<ChessMove> bishop_movement(ChessBoard board, ChessPosition myPosition) {
        // Check moving up
        int x = myPosition.getRow();
        int y = myPosition.getColumn();
        Collection<ChessMove> lst = new ArrayList<ChessMove>();
        for (int i = 1; i < 9-y; i++) {
            // to the left
            if ((x-i) >= 0) {
                ChessPosition end = new ChessPosition(x-i, y + i);
                lst.add((new ChessMove(myPosition, end, null)));
            }
            // to the right
            if ((x+i) <= 8) {
                ChessPosition end = new ChessPosition(x+i, y + i);
                lst.add((new ChessMove(myPosition, end, null)));
            }
        }
        // check 1 down
        for (int i = 1; i < y; i++) {
            // to the left
            if ((x-i) >= 0) {
                ChessPosition end = new ChessPosition(x-i, y-i);
                lst.add((new ChessMove(myPosition, end, null)));
            }
            // to the right
            if ((x+i) <= 8) {
                ChessPosition end = new ChessPosition(x+i, y-i);
                lst.add((new ChessMove(myPosition, end, null)));
            }
        }
        return lst;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}