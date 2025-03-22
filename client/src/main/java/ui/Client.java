package ui;

import chess.request.LoginRequest;
import chess.request.RegisterRequest;
import chess.result.LoginResult;
import chess.result.RegisterResult;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client {

    private Scanner scanner;
    private String authToken;
    private ServerFacade facade;

    public Client(String url) {
        scanner = new Scanner(System.in);
        authToken = null;
        facade = new ServerFacade(url);
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
            throw new InvalidUserInputException("could not process due to " + e.getMessage());
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
            throw new InvalidUserInputException("could not process due to " + e.getMessage());
        }
    }

    private void postLoginRepl() {
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
