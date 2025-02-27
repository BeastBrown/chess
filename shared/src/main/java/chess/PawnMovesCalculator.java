package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.lang.Math;

public class PawnMovesCalculator implements PieceMovesCalculator {

    public boolean isValid(ChessPosition position, ChessPosition scenario, ChessBoard board, ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            return ((position.getRow() - scenario.getRow() == -1) || (position.getRow() == 2 &&
                    board.getPiece(new ChessPosition(position.getRow()+1, position.getColumn())) == null)) &&
                    scenario.inBounds() &&
                    ((board.getPiece(scenario) == null && position.getColumn() - scenario.getColumn() == 0) ||
                            (board.getPiece(scenario) != null &&
                                    board.getPiece(scenario).getTeamColor() == ChessGame.TeamColor.BLACK &&
                                    Math.abs(position.getColumn() - scenario.getColumn()) == 1));
        } else {
            return ((position.getRow() - scenario.getRow() == 1) ||
                    (position.getRow() == 7 &&
                            board.getPiece(new ChessPosition(position.getRow()-1, position.getColumn())) == null)) &&
                    scenario.inBounds() &&
                    ((board.getPiece(scenario) == null && position.getColumn() - scenario.getColumn() == 0) ||
                            (board.getPiece(scenario) != null &&
                                    board.getPiece(scenario).getTeamColor() == ChessGame.TeamColor.WHITE &&
                                    Math.abs(position.getColumn() - scenario.getColumn()) == 1));
        }
    }

    public ArrayList<ChessPosition> getScenarios(ChessPosition position, ChessGame.TeamColor color) {
        ArrayList<ChessPosition> scenarios = new ArrayList<ChessPosition>();
        if (color == ChessGame.TeamColor.WHITE) {
            scenarios.add(new ChessPosition(position.getRow()+2, position.getColumn()));
            scenarios.add(new ChessPosition(position.getRow()+1, position.getColumn()));
            scenarios.add(new ChessPosition(position.getRow()+1, position.getColumn()-1));
            scenarios.add(new ChessPosition(position.getRow()+1, position.getColumn()+1));
        } else {
            scenarios.add(new ChessPosition(position.getRow()-2, position.getColumn()));
            scenarios.add(new ChessPosition(position.getRow()-1, position.getColumn()));
            scenarios.add(new ChessPosition(position.getRow()-1, position.getColumn()-1));
            scenarios.add(new ChessPosition(position.getRow()-1, position.getColumn()+1));
        }
        return scenarios;
    }

    @Override
    public ArrayList<ChessPiece.PieceType> getPromotionTypes(ChessGame.TeamColor color, ChessPosition position) {
        ArrayList<ChessPiece.PieceType> promotionTypes = new ArrayList<ChessPiece.PieceType>();
        if ((ChessGame.TeamColor.WHITE == color && position.getRow() == 7) ||
                (ChessGame.TeamColor.BLACK == color && position.getRow() == 2)) {
            promotionTypes.add(ChessPiece.PieceType.QUEEN);
            promotionTypes.add(ChessPiece.PieceType.ROOK);
            promotionTypes.add(ChessPiece.PieceType.KNIGHT);
            promotionTypes.add(ChessPiece.PieceType.BISHOP);
        } else {
            promotionTypes.add(null);
        }
        return promotionTypes;
    }
}
