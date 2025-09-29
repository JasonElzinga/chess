package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor team;

    public ChessGame() {
        this.team = TeamColor.WHITE;
        this.board = new ChessBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return team;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        if (team == TeamColor.BLACK) {
            team = TeamColor.WHITE;
        }
        else {
            team = TeamColor.BLACK;
        }
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPos) throws CloneNotSupportedException {


        ChessPiece thisPiece = board.getPiece(startPos);
        var posMoves = thisPiece.pieceMoves(board, startPos);
        ChessGame.TeamColor color = thisPiece.getTeamColor();


        for (var posMove : posMoves) {
            ChessBoard tempBoard = board.clone();
            ChessPosition currentPos = new ChessPosition(startPos.getRow(), startPos.getColumn());
            var endPos = new ChessPosition(posMove.getEndPosition().getRow(), posMove.getEndPosition().getColumn());

            tempBoard.addPiece(currentPos, null);
            tempBoard.addPiece(endPos, thisPiece);

            if (isInCheck(color)) {

            }
        }

        ChessPiece piece = board.getPiece((startPos));
        if (piece == null) {
            return null;
        }
        return piece.pieceMoves(board, startPos);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {

        var startPosition = move.getStartPosition();
        ChessPiece thisPiece = board.getPiece(startPosition);


        Collection<ChessMove> posMoves = validMoves(startPosition);
        if (posMoves == null) {
            throw new InvalidMoveException("There is no piece here");
        }


    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheckHelper(teamColor, board);
    }


    public boolean isInCheckHelper(TeamColor teamColor, ChessBoard tempBoard) {
        Collection<ChessMove> allPosMoves = new ArrayList<ChessMove>();
        ChessPosition kingPos = null;

        // get the kingPos and also all the enemy posMoves
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <=8; j++) {
                ChessPosition pos = new ChessPosition(i,j);
                ChessPiece thisPiece = tempBoard.getPiece(pos);

                if (thisPiece != null) {
                    if (thisPiece.getTeamColor() != teamColor) {
                        Collection<ChessMove> posMoves = thisPiece.pieceMoves(tempBoard, pos);
                        allPosMoves.addAll(posMoves);
                    }
                    else {
                        if (thisPiece.getPieceType() == ChessPiece.PieceType.KING) {
                            kingPos = new ChessPosition(i,j);
                        }
                    }
                }
            }
        }
        // check if the king is in check

        for (var move : allPosMoves) {
            if (move.getEndPosition()==kingPos) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param newBoard the new board to use
     */
    public void setBoard(ChessBoard newBoard) {
        this.board = newBoard;
//        for (int i = 1; i <= 8; i++) {
//            for (int j = 1; j <= 8; j++) {
//                ChessPosition pos = new ChessPosition(i, j);
//                ChessPiece thisPiece = board.getPiece(pos);
//
//                if (thisPiece != null) {
//                    newBoard.addPiece(pos, thisPiece);
//                }
//            }
//        }
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }


    @Override
    public String toString() {
        return "ChessGame{}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && team == chessGame.team;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, team);
    }
}
