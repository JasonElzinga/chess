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
            return king_movement(board, pos);
        }
    }



    private Collection<ChessMove> king_movement(ChessBoard board, ChessPosition pos) {
        // Check moving up
        int row = pos.getRow();
        int col = pos.getColumn();
        ChessPiece thisPiece = board.getPiece(pos);
        ChessGame.TeamColor color = thisPiece.getTeamColor();
        Collection<ChessMove> posMoves = new ArrayList<ChessMove>();
        int[][] check_moves = new int[4][4];
        int offset = -1;

        if (color == ChessGame.TeamColor.WHITE) {
            offset = 1;
        }
        // check if it hasn't moved
        if (row == 2 || row==7) {
            check_moves[0][0] = row + (2*offset);
            check_moves[0][1] = col;
            check_moves[1][0] = row + offset;
            check_moves[1][1] = col;
        }
        else {
            check_moves[0][0] = row + offset;
            check_moves[0][1] = col;
        }

        //attack
        check_moves[2][0] = row + offset;
        check_moves[2][1] = col -1;
        check_moves[3][0] = row + offset;
        check_moves[3][1] = col + 1;


        for (int[] direction : check_moves) {
            ChessPosition newLoc = new ChessPosition(direction[0], direction[1]);
            int new_row = direction[0];
            int new_col = direction[1];

            if (new_col > 8 || new_col < 1 || new_row < 1 || new_row > 8) {
                continue;
            } else {
                ChessPiece target = board.getPiece(newLoc);
                if (target == null) {
                    // if attacking but no one there you can't move there
                    if (new_row != row && new_col != col) {
                        continue;
                    }
                }
                // check for color
                else if (color == target.getTeamColor()) {
                    continue;
                }
                else {
                    // can't capture forward
                    if (new_col == col) {
                        continue;
                    }
                }
                if (abs(new_row-row)==2) {
                    ChessPiece targetOneBefore = board.getPiece(new ChessPosition((row+offset),col));
                    if (targetOneBefore != null) {
                        continue;
                    }
                }
                if (new_row == 1 || new_row == 8) {
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