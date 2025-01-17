package chess;
import chess.ChessMove;
import java.util.Collection;

public interface PieceMovesCalculator {
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessGame.TeamColor color, ChessPosition position);
}
