package chess;

import java.util.ArrayList;

public class KnightMovesCalculator implements PieceMovesCalculator {
    @Override
    public boolean isValid(ChessPosition position, ChessPosition scenario, ChessBoard board, ChessGame.TeamColor color) {
        return scenario.inBounds() &&
                (!board.isOccupied(scenario) || board.getPiece(scenario).getTeamColor() != color);
    }

    @Override
    public ArrayList<ChessPosition> getScenarios(ChessPosition position, ChessGame.TeamColor color) {
        ArrayList<ChessPosition> moves = new ArrayList<ChessPosition>();
        for (int i = 1; i < 3; i++) {
            int factor = switch(i) {
                case 2 -> -1;
                default -> 1;
            };
            moves.add(new ChessPosition(position.getRow()+2, position.getColumn()+factor));
            moves.add(new ChessPosition(position.getRow()-2, position.getColumn()+factor));
            moves.add(new ChessPosition(position.getRow()+factor, position.getColumn()+2));
            moves.add(new ChessPosition(position.getRow()+factor, position.getColumn()-2));
        }
        return moves;
    }
}
