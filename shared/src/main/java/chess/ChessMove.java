package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return this.startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return this.endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return this.promotionPiece;
    }

    /**
     * returns whether the ChessMove is equal to another.
     * This has a lengthy compound proposition using XOR at the end to prevent
     * a NullPointerException
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        ChessMove move = (ChessMove) obj;
        return this.startPosition.equals(move.startPosition) &&
                this.endPosition.equals(move.endPosition) &&
                ((!(this.promotionPiece == null ^ move.promotionPiece == null)) &&
                        ((this.promotionPiece == null && move.promotionPiece == null) ||
                                this.promotionPiece.equals(move.promotionPiece)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.startPosition, this.endPosition, this.promotionPiece);
    }
}
