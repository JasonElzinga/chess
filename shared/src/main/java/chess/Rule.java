package chess;

import java.util.ArrayList;
import java.util.Collection;

public class Rule {

    private final boolean repeat;
    private final int[][] directions;

    public Rule(boolean repeat, int[][] directions) {
        this.repeat = repeat;
        this.directions = directions;
    }


    public Collection<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> posMoves = new ArrayList<>();
        ChessPiece thisPiece = board.getPiece(myPosition);
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        for (var direction: directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];
            ChessPosition targetLoc = new ChessPosition(newRow, newCol);

            if (newRow > 8 || newCol > 8 || newRow < 1 || newCol < 1) {
                continue;
            }
            ChessPiece target = board.getPiece(targetLoc);

            if (target !=null) {
                if (target.getTeamColor() == thisPiece.getTeamColor()) {
                    continue;
                }
                else {
                    posMoves.add(new ChessMove(myPosition,targetLoc,null));
                    continue;
                }
            }
            posMoves.add(new ChessMove(myPosition,targetLoc,null));

            if (repeat) {
                while (true) {
                    newRow += direction[0];
                    newCol += direction[1];

                    targetLoc = new ChessPosition(newRow, newCol);

                    if (newRow > 8 || newCol > 8 || newRow < 1 || newCol < 1) {
                        break;
                    }
                    target = board.getPiece(targetLoc);

                    if (target !=null) {
                        if (target.getTeamColor() == thisPiece.getTeamColor()) {
                            break;
                        }
                        else {
                            posMoves.add(new ChessMove(myPosition,targetLoc,null));
                            break;
                        }
                    }
                    posMoves.add(new ChessMove(myPosition,targetLoc,null));

                }
            }
        }
        return posMoves;
    }

}
