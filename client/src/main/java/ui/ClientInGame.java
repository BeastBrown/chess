package ui;

import chess.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Collection;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

public class ClientInGame {

    private static Logger logger = Logger.getGlobal();
    private ChessGame game;
    private ChessGame.TeamColor allegiance;
    private ServerFacade facade;
    private String authToken;
    private Integer gameID;
    private Scanner scanner;

    public ClientInGame(ServerFacade facade, String authToken, Integer gameID) {
        game = null;
        allegiance = null;
        this.facade = facade;
        this.authToken = authToken;
        this.gameID = gameID;
        scanner = new Scanner(System.in);
    }

    public void showNotification(NotificationMessage message) {
        boolean isResignMessage = message.getMessage().contains("resign");
        if (isResignMessage) {
            game.isActive = false;
        }
        System.out.println(message.getMessage());
        printFancyConsole();
    }

    public void loadGame(LoadGameMessage message) {
        game = message.getGame();
        draw();
    }

    public void showError(ErrorMessage message) {
        System.out.println(message.getErrorMessage());
        printFancyConsole();
    }

    private void storeAllegiance(String gameAllegiance) {
        this.allegiance = switch (gameAllegiance) {
            case "WHITE" -> WHITE;
            case "BLACK" -> BLACK;
            default -> null;
        };
    }

    public void inGameTransition(String gameAllegiance) {
        logger.log(Level.INFO, "Entering the in game transition");
        storeAllegiance(gameAllegiance);
        facade.connectToWebsocket(authToken, gameID);
        if (allegiance == null) {
            observationRepl();
        } else {
            gamePlayRepl();
        }
    }

    private void observationRepl() {
        printInObservationHelp();
        String[] args = {"good stuff"};
        while (!args[0].equals("LEAVE")) {
            sleepTime(500);
            printFancyConsole();
            String input = scanner.nextLine().toUpperCase();
            args = input.split("\\s+");
            inObservationArguments(args);
        }
    }

    private void sleepTime(int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "It wouldn't let the Thread sleep");
        }
    }

    private void inObservationArguments(String[] args) {
        String first = args[0];
        switch (first) {
            case "DRAW" -> draw();
            case "LEAVE" -> leave();
            case "HIGHLIGHT" -> highlight(args);
            default -> printInObservationHelp();
        }
    }

    private void printInObservationHelp() {
        String message = """
                help - to display this menu
                draw - redraw the chessboard
                leave - to leave the game
                highlight <piece pos> - to highlight which squares are legal moves
                """;
        System.out.println(message);
    }

    private void gamePlayRepl() {
        printInPlayHelp();
        String[] args = {"good stuff"};
        while (!args[0].equals("LEAVE")) {
            sleepTime(500);
            printFancyConsole();
            String input = scanner.nextLine().toUpperCase();
            args = input.split("\\s+");
            inPlayArguments(args);
        }
    }

    private void inPlayArguments(String[] args) {
        String first = args[0];
        switch (first) {
            case "DRAW" -> draw();
            case "MOVE" -> movePiece(args);
            case "RESIGN" -> resign();
            case "LEAVE" -> leave();
            case "HIGHLIGHT" -> highlight(args);
            default -> printInPlayHelp();
        }
    }

    private void highlight(String[] args) {
        try {
            ChessPosition pos = getPos(args[1]);
            validateIsInGame();
            validatePosBounds(pos);
            validateIsPiece(pos);
            Collection<ChessMove> moves = game.validMoves(pos);
            ChessGame.TeamColor perspective = getPerspective();
            BoardDisplay display = new BoardDisplay(game.getBoard(), perspective, moves);
            display.showBoard();
        } catch (InvalidUserInputException e) {
            System.out.println(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Must supply a position of form [column letter][row number]");
        }
    }

    private void validateIsPiece(ChessPosition pos) throws InvalidUserInputException {
        ChessPiece piece = this.game.getBoard().getPiece(pos);
        if (piece == null) {
            throw new InvalidUserInputException("The position does not correspond to a piece");
        }
    }

    private void validatePosBounds(ChessPosition pos) throws InvalidUserInputException {
        if (!pos.inBounds()) {
            throw new InvalidUserInputException("position out of bounds");
        }
    }

    private void leave() {
        try {
            validateIsInGame();
            facade.leave(authToken, gameID);
        } catch (InvalidUserInputException e) {
            System.out.println(e.getMessage());
        }
    }

    private void resign() {
        if (!confirmResign()) {
            return;
        }
        try {
            validateIsInGame();
            facade.resign(authToken, gameID);
        } catch (InvalidUserInputException e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean confirmResign() {
        System.out.println("Are You sure you want to resign? (Y/N)");
        printFancyConsole();
        String response = scanner.nextLine().toUpperCase();
        return response.equals("Y");
    }

    private void validateIsInGame() throws InvalidUserInputException {
        if (gameID == null) {
            throw new InvalidUserInputException("You need to be in a game first");
        }
    }

    private void movePiece(String[] args) {
        ChessMove move = null;
        try {
            validateIsActive();
            move = getMove(args);
            facade.makeMove(authToken, gameID, move);
        } catch (InvalidUserInputException e) {
            System.out.println(e.getMessage());
        }
    }

    private void validateIsActive() throws InvalidUserInputException {
        validateIsInGame();
        if (!game.isActive) {
            throw new InvalidUserInputException("The Game is No longer Active");
        }
    }

    private ChessMove getMove(String[] args) throws InvalidUserInputException {
        ChessPosition from = null;
        ChessPosition to = null;
        try {
            from = getPos(args[1]);
            to = getPos(args[2]);
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidUserInputException("You must provide from and to positions");
        }
        ChessPiece.PieceType promotion = null;
        try {
            promotion = args.length >= 4 ? ChessPiece.PieceType.valueOf(args[3]) : null;
        } catch (IllegalArgumentException e) {
            throw new InvalidUserInputException("Your promotion piece is not an actual chess piece");
        }
        ChessMove move = new ChessMove(from, to, promotion);
        validatePosBounds(from);
        validateIsPiece(from);
        Collection<ChessMove> legitMoves = game.validMoves(from);
        if (!legitMoves.contains(move)) {
            throw new InvalidUserInputException("Your move is invalid, highlight to show valid moves");
        }
        return move;
    }

    private ChessPosition getPos(String arg) throws InvalidUserInputException {
        try {
            String colLetter = arg.substring(0,1);
            Integer colNumber = getColNumber(colLetter);
            Integer rowNumber = (Integer) Integer.parseInt(arg.substring(1,2));
            return new ChessPosition(rowNumber, colNumber);
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw new InvalidUserInputException("Positions must be in the format [column letter][row number]");
        }
    }

    private Integer getColNumber(String colLetter) throws InvalidUserInputException {
        return switch (colLetter) {
            case "A" -> 1;
            case "B" -> 2;
            case "C" -> 3;
            case "D" -> 4;
            case "E" -> 5;
            case "F" -> 6;
            case "G" -> 7;
            case "H" -> 8;
            default -> throw new InvalidUserInputException(
                    "The first part of the position needs the column letter");
        };
    }

    private void draw() {
        ChessGame.TeamColor perspective = getPerspective();
        if (game != null) {
            ChessBoard board = game.getBoard();
            new BoardDisplay(board ,perspective).showBoard();
        } else {
            System.out.println("There isn't a game to show yet!");
        }
    }

    private ChessGame.TeamColor getPerspective() {
        return allegiance == null ? WHITE : allegiance;
    }

    private void printInPlayHelp() {
        String message = """
                help - to display this menu
                draw - redraw the chessboard
                move <from pos> <to pos> <promotion piece if applicable> - to move a piece
                resign - to forfeit to match
                leave - to leave the game
                highlight <piece pos> - to highlight which squares are legal moves
                """;
        System.out.println(message);
    }

    private void printFancyConsole() {
        System.out.print(">>> ");
    }
}
