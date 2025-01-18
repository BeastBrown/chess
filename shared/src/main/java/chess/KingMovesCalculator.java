package chess;
import jdk.jshell.spi.ExecutionControl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class KingMovesCalculator implements PieceMovesCalculator {
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessGame.TeamColor color, ChessPosition position) {
        ArrayList<ChessPosition> scenarios = getScenarios(position);
        Collection<ChessMove> moves = new HashSet<ChessMove>();

        for (ChessPosition scenario : scenarios) {
            if (isValid(scenario, board, color)) {
                moves.add(new ChessMove(position, scenario, null));
            }
        }
        return moves;
    }

    private boolean isValid(ChessPosition scenario, ChessBoard board, ChessGame.TeamColor color) {
        return scenario.inBounds() &&
                ((!board.isOccupied(scenario)) || board.getPiece(scenario).getTeamColor() != color);
    }

    private ArrayList<ChessPosition> getScenarios(ChessPosition position) {
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
