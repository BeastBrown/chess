import chess.*;
import server.Server;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        Logger logger = Logger.getGlobal();
        logger.setLevel(Level.ALL);
        ConsoleHandler cHandler = new ConsoleHandler();
        cHandler.setLevel(Level.ALL);
        logger.addHandler(cHandler);
        logger.log(Level.FINE, "Logging start");

        Server server = new Server();
        server.run(8080);
    }
}