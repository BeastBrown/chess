package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.data.GameData;
import chess.request.*;
import chess.result.ListGameResult;
import chess.result.LoginResult;
import chess.result.RegisterResult;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

public class Client {

    private Scanner scanner;
    private String authToken;
    private ServerFacade facade;
    private HashSet<Integer> IDSet;

    public Client(String url) {
        scanner = new Scanner(System.in);
        authToken = null;
        facade = new ServerFacade(url);
        IDSet = new HashSet<Integer>();
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
        String[] args = input.split(" ");
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
        String[] args = input.split(" ");
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
        while (!input.equals("logout")) {
            printFancyConsole();
            input = scanner.nextLine();
            postLoginArguments(input);
        }
    }

    private void postLoginArguments(String input) {
        String[] args = input.split(" ");
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
        String[] args = input.split(" ");
        if (args.length < 2) {
            throw new InvalidUserInputException("usage: observe <Game ID #>");
        }
        Integer gameID = Integer.parseInt(args[1]);
        if (!IDSet.contains(gameID)) {
            throw new InvalidUserInputException("The game ID does not correspond to a game. " +
                    "You must list the games");
        }
        inGameRepl("WHITE");
    }

    private void facilitatePlay(String input) {
        try {
            playGame(input);
        } catch (InvalidUserInputException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void playGame(String input) throws InvalidUserInputException, IOException {
        String[] args = input.split(" ");
        if (args.length < 3) {
            throw new InvalidUserInputException("usage: play <game ID #> <desired color>");
        }
        String desiredColor = args[2];
        int gameID = Integer.parseInt(args[1]);
        JoinGameRequest req = new JoinGameRequest(this.authToken, desiredColor, gameID);
        facade.joinGame(req);
        inGameRepl(desiredColor);
    }

    private void inGameRepl(String color) {
        ChessGame.TeamColor allegiance = color.equals("WHITE") ? WHITE : BLACK;
        new BoardDisplay(new ChessBoard(), allegiance).showBoard();
        String message = """
                Congratulations on making it to the Game!
                You will be redirected to the post login UI""";
        System.out.println(message);
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
            this.IDSet.add(game.gameID());
        }
    }

    private void displayGame(GameData game) {
        String whitePlayer = game.whiteUsername() == null ? "FREE" : game.whiteUsername();
        String blackPlayer = game.whiteUsername() == null ? "FREE" : game.blackUsername();
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
        String[] args = input.split(" ");
        if (args.length < 2) {
            throw new InvalidUserInputException("usage: register <username> <password> <email>");
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
}
