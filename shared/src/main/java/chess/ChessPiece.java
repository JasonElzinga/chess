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
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition pos) {
        ChessPiece target = board.getPiece(pos);
        if (target.getPieceType() != PieceType.PAWN) {
            Rule rule = switch (getPieceType()) {
                case BISHOP -> new Rule(true, new int[][]{{1,-1}, {-1,1},{-1,-1},{1,1}});
                case ROOK -> new Rule(true, new int[][]{{1,0}, {-1,0},{0,1},{0,-1}});
                case KNIGHT -> new Rule(false, new int[][]{{2,1},{-2,1},{2,-1},{-2,-1},{1,2}, {-1,2},{1,-2},{-1,-2}});
                case QUEEN -> new Rule(true, new int[][]{{1,0}, {-1,0},{0,1},{0,-1},{1,-1}, {-1,1},{-1,-1},{1,1}});
                case KING -> new Rule(false, new int[][]{{1,0}, {-1,0},{0,1},{0,-1},{1,-1}, {-1,1},{-1,-1},{1,1}});
                default -> null;
            };
            return rule.getMoves(board, pos);
        }
        else {
            return pawnMovement(board, pos);
        }
    }

    private Collection<ChessMove> pawnMovement(ChessBoard board, ChessPosition pos) {
        // Check moving up
        int row = pos.getRow();
        int col = pos.getColumn();
        ChessPiece thisPiece = board.getPiece(pos);
        ChessGame.TeamColor color = thisPiece.getTeamColor();
        Collection<ChessMove> posMoves = new ArrayList<ChessMove>();
        int[][] checkMoves = new int[4][4];
        int offset = -1;

        if (color == ChessGame.TeamColor.WHITE) {
            offset = 1;
        }
        // check if it hasn't moved
        if (row == 2 || row==7) {
            checkMoves[0][0] = row + (2*offset);
            checkMoves[0][1] = col;
            checkMoves[1][0] = row + offset;
            checkMoves[1][1] = col;
        }
        else {
            checkMoves[0][0] = row + offset;
            checkMoves[0][1] = col;
        }

        //attack
        checkMoves[2][0] = row + offset;
        checkMoves[2][1] = col -1;
        checkMoves[3][0] = row + offset;
        checkMoves[3][1] = col + 1;


        for (int[] direction : checkMoves) {
            ChessPosition newLoc = new ChessPosition(direction[0], direction[1]);
            int newRow = direction[0];
            int newCol = direction[1];

            if (newCol > 8 || newCol < 1 || newRow < 1 || newRow > 8) {
                continue;
            } else {
                ChessPiece target = board.getPiece(newLoc);
                if (target == null) {
                    // if attacking but no one there you can't move there
                    if (newRow != row && newCol != col) {
                        continue;
                    }
                }
                // check for color
                else if (color == target.getTeamColor()) {
                    continue;
                }
                else {
                    // can't capture forward
                    if (newCol == col) {
                        continue;
                    }
                }
                if (abs(newRow-row)==2) {
                    ChessPiece targetOneBefore = board.getPiece(new ChessPosition((row+offset),col));
                    if (targetOneBefore != null) {
                        continue;
                    }
                }
                if (newRow == 1 || newRow == 8) {
                    posMoves.add(new ChessMove(pos, newLoc, ChessPiece.PieceType.ROOK));
                    posMoves.add(new ChessMove(pos, newLoc, ChessPiece.PieceType.KNIGHT));
                    posMoves.add(new ChessMove(pos, newLoc, ChessPiece.PieceType.BISHOP));
                    posMoves.add(new ChessMove(pos, newLoc, ChessPiece.PieceType.QUEEN));
                }
                else {
                    posMoves.add(new ChessMove(pos, newLoc, null));
                }
            }
        }
        return posMoves;
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