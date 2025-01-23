package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor teamColor;
    private PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.pieceType = type;
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
        return this.teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        switch (this.pieceType) {
            case KING:
                return new KingMovesCalculator().calculateMoves(board, this.teamColor, myPosition);
            case PAWN:
                return new PawnMovesCalculator().calculateMoves(board, this.teamColor, myPosition);
            case BISHOP:
                return new BishopMovesCalculator().calculateMoves(board, this.teamColor, myPosition);
            case ROOK:
                return new RookMovesCalculator().calculateMoves(board, this.teamColor, myPosition);
        }
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean equals(Object obj) {
        ChessPiece piece = (ChessPiece) obj;
        if (piece == null) {return false;}
        return this.pieceType == piece.pieceType && this.teamColor == piece.teamColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pieceType, this.teamColor);
    }
}
