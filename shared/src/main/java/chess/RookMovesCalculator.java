package chess;

import java.util.ArrayList;

public class RookMovesCalculator implements PieceMovesCalculator {

    @Override
    public boolean isValid(ChessPosition position, ChessPosition scenario, ChessBoard board, ChessGame.TeamColor color) {
        return scenario.inBounds() &&
                isClear(position, scenario, board) &&
                (!board.isOccupied(scenario) || board.getPiece(scenario).getTeamColor() != color);
    }

    private boolean isClear(ChessPosition startPosition, ChessPosition endPosition, ChessBoard board) {
        for (int i = startPosition.getRow(), j = startPosition.getColumn(); i != endPosition.getRow() || j != endPosition.getColumn(); i = iterate(i, endPosition.getRow()), j = iterate(j, endPosition.getColumn())) {
            if (board.isOccupied(new ChessPosition(i, j)) && (i!= startPosition.getRow() || j != startPosition.getColumn())) {
                return false;
            }
        }
        return true;
    }

    private int iterate(int val, int last) {
        if (last - val > 0) {
            val++;
        } else if (last - val < 0) {
            val--;
        } // if its equal
        return val;
    }

    @Override
    public ArrayList<ChessPosition> getScenarios(ChessPosition position, ChessGame.TeamColor color) {
        ArrayList<ChessPosition> scenarios = new ArrayList<ChessPosition>();
        for (int i = 1; i < 9; i++) {
            scenarios.add(new ChessPosition(position.getRow()+i, position.getColumn()));
            scenarios.add(new ChessPosition(position.getRow()-i, position.getColumn()));
            scenarios.add(new ChessPosition(position.getRow(), position.getColumn()+i));
            scenarios.add(new ChessPosition(position.getRow(), position.getColumn()-i));
        }
        return scenarios;
    }
}
