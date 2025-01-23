package chess;
import jdk.jshell.spi.ExecutionControl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class KingMovesCalculator implements PieceMovesCalculator {

    public boolean isValid(ChessPosition position, ChessPosition scenario, ChessBoard board, ChessGame.TeamColor color) {
        return scenario.inBounds() &&
                ((!board.isOccupied(scenario)) || board.getPiece(scenario).getTeamColor() != color);
    }

    @Override
    public ArrayList<ChessPosition> getScenarios(ChessPosition position, ChessGame.TeamColor color) {
        ArrayList<ChessPosition> scenarios = new ArrayList<ChessPosition>();
        scenarios.add(new ChessPosition(position.getRow()-1, position.getColumn()));
        scenarios.add(new ChessPosition(position.getRow()+1, position.getColumn()));
        scenarios.add(new ChessPosition(position.getRow(), position.getColumn()-1));
        scenarios.add(new ChessPosition(position.getRow(), position.getColumn()+1));
        scenarios.add(new ChessPosition(position.getRow()+1, position.getColumn()+1));
        scenarios.add(new ChessPosition(position.getRow()-1, position.getColumn()-1));
        scenarios.add(new ChessPosition(position.getRow()-1, position.getColumn()+1));
        scenarios.add(new ChessPosition(position.getRow()+1, position.getColumn()-1));
        return scenarios;
    }
}
