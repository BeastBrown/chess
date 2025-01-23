package chess;

import java.util.ArrayList;

public class QueenMovesCalculator implements PieceMovesCalculator {

    @Override
    public boolean isValid(ChessPosition position, ChessPosition scenario, ChessBoard board, ChessGame.TeamColor color) {
        if (isRookMove(position, scenario)) {
            return RookMovesCalculator.staticValid(position, scenario, board, color);
        }

        return BishopMovesCalculator.staticValid(position, scenario, board, color);
    }

    private boolean isRookMove(ChessPosition firstPosition, ChessPosition lastPosition) {
        return firstPosition.getRow() == lastPosition.getRow() || firstPosition.getColumn() == lastPosition.getColumn();
    }

    @Override
    public ArrayList<ChessPosition> getScenarios(ChessPosition position, ChessGame.TeamColor color) {
        ArrayList<ChessPosition> first = RookMovesCalculator.staticScenarios(position, color);
        first.addAll(BishopMovesCalculator.staticScenarios(position, color));
        return first;
    }
}
