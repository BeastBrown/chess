import chess.*;
import ui.BoardDisplay;
import ui.Client;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static chess.ChessGame.TeamColor.WHITE;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        configureLogging();
        new Client("http://localhost:8080").run();
    }

    private static void configureLogging() {
        Logger logger = Logger.getGlobal();
        logger.setLevel(Level.OFF);
    }
}