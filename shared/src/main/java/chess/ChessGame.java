package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessGame.TeamColor teamTurn;
    private ChessBoard board;


    public ChessGame() {
        this.teamTurn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
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
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> validatedMoves = new HashSet<ChessMove>();
        ChessPiece piece = this.board.getPiece(startPosition);
        Collection<ChessMove> moves = piece.pieceMoves(this.board, startPosition);
        ChessGame.TeamColor color = piece.getTeamColor();
        for (ChessMove move : moves) {
            try {
                ChessGame newGame = (ChessGame) this.clone();
                newGame.board.executeMove(move);
                if (!newGame.isInCheck(color)) {validatedMoves.add(move);}
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
        return validatedMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> legitimateMoves = validMoves(move.getStartPosition());
        if (legitimateMoves.contains(move)) {
            this.board.executeMove(move);
        } else {
            throw new InvalidMoveException("Move is not Valid");
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = this.board.getKingPos(teamColor);
        Collection<ChessPosition> opposingPositions = this.board.getOpposingThreatPositions(teamColor);
        return opposingPositions.contains(kingPos);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        ChessPosition kingPos = this.board.getKingPos(teamColor);
        return isInCheck(teamColor) &&
                validMoves(kingPos).isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        HashSet<ChessMove> allValidMoves = new HashSet<ChessMove>();
        Collection<ChessPosition> friendlyPositions = this.board.getFriendlyPositions(teamColor);
        for (ChessPosition position : friendlyPositions) {
            allValidMoves.addAll(validMoves(position));
        }
        return !isInCheck(teamColor) &&
                allValidMoves.isEmpty();
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        try {
            this.board = (ChessBoard) board.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        ChessGame clonedGame = new ChessGame();
        clonedGame.teamTurn = this.teamTurn;
        clonedGame.board = (ChessBoard) this.board.clone();
        return clonedGame;
    }
}
