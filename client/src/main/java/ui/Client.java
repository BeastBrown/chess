package ui;

import chess.*;
import chess.data.GameData;
import chess.request.*;
import chess.result.JoinGameResult;
import chess.result.ListGameResult;
import chess.result.LoginResult;
import chess.result.RegisterResult;
import chess.ChessPiece.PieceType;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static Logger logger = Logger.getGlobal();
    private ClientInGame clientInGame;

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
        logger.log(Level.INFO, "Entering the post login arguments");
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
        this.gameID = gameID;
        logger.log(Level.INFO, "Successfully transitioning to observe");

        clientInGame = new ClientInGame(facade, authToken, gameID);
        clientInGame.inGameTransition("OBSERVE");
    }

    private void facilitatePlay(String input) {
        logger.log(Level.INFO, "Entering play facilitation");
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
        logger.log(Level.INFO, "Transitioning to play game");
        clientInGame = new ClientInGame(facade, authToken, gameID);
        clientInGame.inGameTransition(desiredColor);
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
        logger.log(Level.INFO, "Entering the notify method");
        switch (message.getServerMessageType()) {
            case ERROR -> clientInGame.showError((ErrorMessage) message);
            case LOAD_GAME -> clientInGame.loadGame((LoadGameMessage) message);
            case NOTIFICATION -> clientInGame.showNotification((NotificationMessage) message);
        }
    }
}
