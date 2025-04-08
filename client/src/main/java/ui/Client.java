package ui;

import chess.*;
import chess.data.GameData;
import chess.request.*;
import chess.result.JoinGameResult;
import chess.result.ListGameResult;
import chess.result.LoginResult;
import chess.result.RegisterResult;
import chess.ChessPiece.PieceType;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

public class Client implements ServerMessageObserver {

    private Scanner scanner;
    private String authToken;
    private ServerFacade facade;
    private HashSet<Integer> idSet;
    private ChessGame.TeamColor allegiance;
    private Integer gameID;
    private ChessGame game;

    public Client(String url) {
        scanner = new Scanner(System.in);
        authToken = null;
        facade = new ServerFacade(url, this);
        idSet = new HashSet<Integer>();
        allegiance = null;
        gameID = null;
    }

    public void run() {
        System.out.println("Hello, welcome to the chess client, type help for options");
        String input = "";
        while (!input.equals("quit")) {
            printFancyConsole();
            input = scanner.nextLine();
            preLoginArguments(input);
        }
    }

    private void preLoginArguments(String input) {
        if (input.equals("help")) {
            printPreLoginHelp();
        } else if (input.startsWith("register")) {
            processRegistration(input);
        } else if (input.startsWith("login")) {
            processLogin(input);
        } else if (!input.equals("quit")) {
            printPreLoginHelp();
        }
    }

    private void processLogin(String input) {
        try {
            loginUser(input);
        } catch (InvalidUserInputException e) {
            System.out.println(e.getMessage());
        }
    }

    private void processRegistration(String input) {
        try {
            registerUser(input);
        } catch (InvalidUserInputException e) {
            System.out.println(e.getMessage());
        }
    }

    private void loginUser(String input) throws InvalidUserInputException {
        String[] args = input.split("\\s+");
        if (args.length < 3) {
            throw new InvalidUserInputException("usage: login <username> <password>");
        }
        LoginRequest req = new LoginRequest(args[1], args[2]);

        try {
            LoginResult res = facade.loginUser(req);
            this.authToken = res.authToken();
            postLoginRepl();
        } catch (IOException e) {
            throw new InvalidUserInputException(e.getMessage());
        }
    }

    private void registerUser(String input) throws InvalidUserInputException {
        String[] args = input.split("\\s+");
        if (args.length < 4) {
            throw new InvalidUserInputException("usage: register <username> <password> <email>");
        }
        RegisterRequest req = new RegisterRequest(args[1], args[2], args[3]);
        try {
            RegisterResult res = facade.registerUser(req);
            this.authToken = res.authToken();
            postLoginRepl();
        } catch (IOException e) {
            throw new InvalidUserInputException(e.getMessage());
        }
    }

    private void postLoginRepl() {
        System.out.println("Congratulations on logging in! Type help for options");
        String input = "";
        String first = input;
        while (!first.equals("logout")) {
            printFancyConsole();
            input = scanner.nextLine();
            String[] args = input.split("\\s+");
            first = args[0];
            postLoginArguments(input);
        }
    }

    private void postLoginArguments(String input) {
        String[] args = input.split("\\s+");
        String first = args[0];
        switch (first) {
            case "logout" -> facilitateLogout(input);
            case "create" -> facilitateCreate(input);
            case "list" -> facilitateList();
            case "play" -> facilitatePlay(input);
            case "observe" -> facilitateObserve(input);
            default -> printPostLoginHelp();
        }
    }

    private void facilitateObserve(String input) {
        try {
            observeGame(input);
        } catch (InvalidUserInputException e) {
            System.out.println(e.getMessage());
        }
    }

    private void observeGame(String input) throws InvalidUserInputException {
        String[] args = input.split("\\s+");
        if (args.length < 2) {
            throw new InvalidUserInputException("usage: observe <Game ID #>");
        }
        Integer gameID = Integer.parseInt(args[1]);
        if (!idSet.contains(gameID)) {
            throw new InvalidUserInputException("The game ID does not correspond to a game. " +
                    "You must list the games");
        }
        inGameTransition("OBSERVE");
    }

    private void facilitatePlay(String input) {
        try {
            playGame(input);
        } catch (InvalidUserInputException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void playGame(String input) throws InvalidUserInputException, IOException {
        String[] args = input.split("\\s+");
        sanitizePlayArgs(args);
        String desiredColor = args[2].toUpperCase();
        int gameID = Integer.parseInt(args[1]);
        JoinGameRequest req = new JoinGameRequest(this.authToken, desiredColor, gameID);
        JoinGameResult res = facade.joinGame(req);
        this.gameID = gameID;
        inGameTransition(desiredColor);
    }

    private void sanitizePlayArgs(String[] args) throws InvalidUserInputException {
        String message = "usage: play <game ID #> <desired color>";
        boolean notEnoughArgs = args.length < 3;
        if (notEnoughArgs) {
            throw new InvalidUserInputException(message);
        }
        String desiredColor = args[2].toUpperCase();
        boolean colorInvalid = !(desiredColor.equals("WHITE") || desiredColor.equals("BLACK"));
        if (colorInvalid) {
            throw new InvalidUserInputException(message);
        }
        Integer gameID = null;
        try {
            gameID = Integer.parseInt((args[1]));
        } catch (NumberFormatException e) {
            throw new InvalidUserInputException(message);
        }
        if (!idSet.contains(gameID)) {
            throw new InvalidUserInputException("The ID " + String.valueOf(gameID) + " is " +
                    "not a valid gameID, list the games to get them assigned");
        }
    }

    private void facilitateList() {
        try {
            listGames();
        } catch (IOException e) {
            System.out.println("This is what the server said went wrong" + e.getMessage());
        }
    }

    private void listGames() throws IOException {
        ListGameRequest req = new ListGameRequest(this.authToken);
        ListGameResult res = facade.listGames(req);
        for (GameData game : res.games()) {
            displayGame(game);
            this.idSet.add(game.gameID());
        }
    }

    private void displayGame(GameData game) {
        String whitePlayer = game.whiteUsername() == null ? "FREE" : game.whiteUsername();
        String blackPlayer = game.blackUsername() == null ? "FREE" : game.blackUsername();
        String message = "Game ID = " + String.valueOf(game.gameID()) + " | " +
                "Game Name = " + game.gameName() + " | " +
                "White Player = " + whitePlayer + " | " +
                "Black Player = " + blackPlayer;
        System.out.println(message);
    }

    private void facilitateCreate(String input) {
        try {
            createGame(input);
        } catch (InvalidUserInputException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("This is what the server said went wrong" + e.getMessage());
        }
    }

    private void createGame(String input) throws InvalidUserInputException, IOException {
        String[] args = input.split("\\s+");
        if (args.length < 2) {
            throw new InvalidUserInputException("usage: create <game name>");
        }
        CreateGameRequest req = new CreateGameRequest(this.authToken, args[1]);
        facade.createGame(req);
    }

    private void facilitateLogout(String input) {
        try {
            logoutUser(input);
        } catch (IOException e) {
            System.out.println("This is what the server said went wrong" + e.getMessage());
        }
    }

    private void logoutUser(String input) throws IOException {
        LogoutRequest req = new LogoutRequest(this.authToken);
        facade.logoutUser(req);
        System.out.println("Successfully logged out");
    }

    private void printPostLoginHelp() {
        String message = """
                help - to print this menu
                logout - to logout
                create <game name> - to create a game
                list - to list current games
                play <game ID #> <desired color> - to play the corresponding game
                observe <game ID #> to observe the corresponding game""";
        System.out.println(message);
    }

    private void printPreLoginHelp() {
        String message ="""
                quit - to quit the application
                help - display the help menu
                register <username> <password> <email> - to register an account
                login <username> <password> - to login
                """;
        System.out.print(message);
    }

    private void printFancyConsole() {
        System.out.print(">>> ");
    }

    @Override
    public void notify(ServerMessage message) {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    private void storeAllegiance(String gameAllegiance) {
        this.allegiance = switch (gameAllegiance) {
            case "WHITE" -> WHITE;
            case "BLACK" -> BLACK;
            default -> null;
        };
    }

    private void inGameTransition(String gameAllegiance) {
        storeAllegiance(gameAllegiance);
        facade.connectToWebsocket(authToken, gameID);
        if (allegiance == null) {
            observationRepl();
        } else {
            gamePlayRepl();
        }
    }

    private void gamePlayRepl() {
        printInPlayHelp();
        String[] args = {"good stuff"};
        while (!args[0].equals("LEAVE")) {
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

    private void movePiece(String[] args) {
        ChessMove move = null;
        try {
            move = getMove(args);
        } catch (InvalidUserInputException e) {
            System.out.println(e.getMessage());
        }
        facade.makeMove(authToken, gameID, move);
    }

    private ChessMove getMove(String[] args) throws InvalidUserInputException {
        ChessPosition from = getPos(args[1]);
        ChessPosition to = getPos(args[2]);
        PieceType promotion = null;
        try {
            promotion = args.length >= 4 ? PieceType.valueOf(args[3]) : null;
        } catch (IllegalArgumentException e) {
            throw new InvalidUserInputException("Your promotion piece is not an actual chess piece");
        }
        ChessMove move = new ChessMove(from, to, promotion);
        Collection<ChessMove> legitMoves = game.validMoves(from);
        if (!legitMoves.contains(move)) {
            throw new InvalidUserInputException("Your move is invalid, highlight to show valid moves")
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
            throw new InvalidUserInputException("Positions must be in the format [column letter][row number]")
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
        ChessGame.TeamColor perspective = allegiance == null ? WHITE : allegiance;
        if (game != null) {
             ChessBoard board = game.getBoard();
             new BoardDisplay(board ,perspective).showBoard();
        } else {
            System.out.println("There isn't a game to show yet!");
        }
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
}
