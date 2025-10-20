package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Rule {

    private final boolean repeat;
    private final int[][] directions;

    public Rule(boolean repeat, int[][] directions) {
        this.repeat = repeat;
        this.directions = directions;
    }


    public Collection<ChessMove> getMoves(ChessBoard board, ChessPosition pos) {
        Collection<ChessMove> posMoves = new ArrayList<ChessMove>();
        ChessPiece thisPiece = board.getPiece(pos);

        int col = pos.getColumn();
        int row = pos.getRow();

            // pieces that move once
            for (int[] direction : directions) {
                ChessPosition newLoc = new ChessPosition(row + direction[0], col + direction[1]);
                int new_row = row + direction[0];
                int new_col = col + direction[1];

                // check bounds
                if (new_col > 8 || new_col < 1 || new_row < 1 || new_row > 8) {
                    continue;
                } else {
                    // check if the target location as an enemy piece
                    ChessPiece target = board.getPiece(newLoc);
                    if (target != null) {
                        if (thisPiece.getTeamColor() != target.getTeamColor()) {
                            posMoves.add(new ChessMove(pos, newLoc, null));
                            continue;
                        }
                        else continue;
                    }
                    posMoves.add(new ChessMove(pos, newLoc, null));
                }

                // check for moves for pieces that move more than once
                if (repeat) {
                    //int new_row  = new_row + direction[0];
                    //int new_col = new_col + direction[1];
                    while (true) {
                        new_row += direction[0];
                        new_col += direction[1];
                        ChessPosition recursiveLoc = new ChessPosition(new_row, new_col);
                        if (new_col > 8 || new_col < 1 || new_row > 8 || new_row < 1) break;

                        else {
                            ChessPiece target = board.getPiece(recursiveLoc);
                            if (target != null) {
                                if (thisPiece.getTeamColor() != target.getTeamColor()) {
                                    posMoves.add(new ChessMove(pos, recursiveLoc, null));
                                    break;
                                }
                                break;
                            }
                        }
                        posMoves.add(new ChessMove(pos, recursiveLoc, null));
                    }
                }
            }
        return posMoves;
    }
}
