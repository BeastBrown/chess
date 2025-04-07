import chess.*;
import ui.BoardDisplay;
import ui.Client;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        Logger logger = Logger.getGlobal();
        logger.setLevel(Level.ALL);
        ConsoleHandler cHandler = new ConsoleHandler();
        cHandler.setLevel(Level.ALL);
        logger.addHandler(cHandler);
        logger.log(Level.FINE, "Logging start");

        new Client("http://localhost:8080").run();
    }
}