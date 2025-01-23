package chess;
import chess.ChessMove;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;

public interface PieceMovesCalculator {
    public default Collection<ChessMove> calculateMoves(ChessBoard board, ChessGame.TeamColor color, ChessPosition position) {
        HashSet<ChessMove> moves = new HashSet<ChessMove>();
        ArrayList<ChessPosition> scenarios = getScenarios(position, color);
        ArrayList<ChessPiece.PieceType> promotionTypes = getPromotionTypes(color, position);
        for (ChessPosition scenario : scenarios) {
            for (ChessPiece.PieceType promotionType : promotionTypes) {
                if (isValid(position, scenario, board, color)) {moves.add(new ChessMove(position, scenario, promotionType));}
            }
        }
        return moves;
    };

    public boolean isValid(ChessPosition position, ChessPosition scenario, ChessBoard board, ChessGame.TeamColor color);

    public ArrayList<ChessPosition> getScenarios(ChessPosition position, ChessGame.TeamColor color);

    public default ArrayList<ChessPiece.PieceType> getPromotionTypes(ChessGame.TeamColor color, ChessPosition position) {
        ArrayList<ChessPiece.PieceType> types = new ArrayList<ChessPiece.PieceType>();
        types.add(null);
        return types;
    }
}