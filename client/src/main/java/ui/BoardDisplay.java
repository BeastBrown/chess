package ui;

import chess.ChessBoard;
import chess.ChessGame.TeamColor;
import chess.ChessPiece;
import chess.ChessPosition;

import static chess.ChessGame.TeamColor.WHITE;
import static ui.EscapeSequences.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BoardDisplay {

    private ChessBoard board;
    private TeamColor allegiance;
    private PrintStream output;
    private static String borderColor = SET_BG_COLOR_YELLOW;
    private static String borderTextColor = SET_TEXT_COLOR_RED;
    private static String darkColor = SET_BG_COLOR_DARK_GREY;
    private static String lightColor = SET_BG_COLOR_LIGHT_GREY;
    private static String whiteColor = SET_TEXT_COLOR_WHITE;
    private static String blackColor = SET_TEXT_COLOR_BLACK;

    public BoardDisplay(ChessBoard board, TeamColor allegiance) {
        this.board = board;
        this.allegiance = allegiance;
        output = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    }

    public void showBoard() {
        drawBorder();
        drawBody();
        drawBorder();
    }

    private void drawBody() {
        List<String> rows = List.of(" 1 ", " 2 ", " 3 ", " 4 "," 5 "," 6 "," 7 "," 8 ");
        if (allegiance == WHITE) {
            for (int i = 8; i > 0; i--) {
                drawCompleteRow(rows, i);
            }
        } else {
            for (int i = 1; i < 9; i++) {
                drawCompleteRow(rows, i);
            }
        }
    }

    private void drawCompleteRow(List<String> rows, int i) {
        output.print(borderColor);
        output.print(borderTextColor);
        output.print(rows.get(i -1));
        drawRow(i);
        output.print(borderColor);
        output.print(borderTextColor);
        output.print(rows.get(i -1));
        output.print(RESET_BG_COLOR);
        output.print("\n");
    }

    private void drawRow(int row) {
        String currBg = Math.floorMod(row, 2) == 1 ? darkColor : lightColor;
        for (int j=1; j < 9 ; j++) {
            output.print(currBg);
            String squareString = getSquareString(new ChessPosition(row, j));
            output.print(squareString);
            currBg = currBg.equals(lightColor) ? darkColor : lightColor;
        }
    }

    private String getSquareString(ChessPosition pos) {
        ChessPiece piece = board.getPiece(pos);
        return piece == null ? "   " : getPieceString(piece);
    }

    private String getPieceString(ChessPiece piece) {
        TeamColor teamColor = piece.getTeamColor();
        boolean isWht = teamColor == WHITE;
        String teamColorString = isWht ? whiteColor : blackColor;
        String pieceClassString = switch(piece.getPieceType()) {
            case KING -> " K ";
            case QUEEN -> " Q ";
            case BISHOP -> " B ";
            case ROOK -> " R ";
            case KNIGHT -> " N ";
            case PAWN -> " P ";
        };
        return teamColorString + pieceClassString;
    }

    private void drawBorder() {
        List<String> border = List.of(" A "," B ", " C "," D "," E "," F "," G "," H ");
        border = allegiance == WHITE ? border : border.reversed();
        output.print(ERASE_SCREEN);
        output.print(borderColor);
        output.print(borderTextColor);
        output.print("   ");
        for (String let : border) {
            output.print(let);
        }
        output.print("   ");
        output.print(RESET_BG_COLOR);
        output.print("\n");
    }
}
