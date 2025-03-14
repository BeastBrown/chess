package ui;

import chess.ChessBoard;
import chess.ChessGame.TeamColor;
import static ui.EscapeSequences.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BoardDisplay {

    private ChessBoard board;
    private TeamColor allegiance;
    private PrintStream output;

    public BoardDisplay(ChessBoard board, TeamColor allegiance) {
        this.board = board;
        this.allegiance = allegiance;
        output = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    }

    public void showBoard() {
        drawBorder();
    }

    private void drawBorder() {
        List<String> border = List.of(" A "," B ", " C "," D "," E "," F "," G "," H ");
        output.print(ERASE_SCREEN);
        output.print(SET_BG_COLOR_YELLOW);
        output.print(SET_TEXT_COLOR_RED);
        output.print(EMPTY);
        for (String lit : border) {
            output.print(lit);
        }
        output.print(EMPTY);
    }
}
