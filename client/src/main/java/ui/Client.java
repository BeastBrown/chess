package ui;

import chess.request.RegisterRequest;
import chess.result.RegisterResult;

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
            registerUser(input);
        } else if (input.startsWith("login")) {
            loginUser(input);
        } else if (!input.equals("quit")) {
            printPreLoginHelp();
        }
    }

    private void loginUser(String input) {
    }

    private void registerUser(String input) {
        String[] args = input.split(" ");
        if (args.length < 4) {
            throw new IllegalArgumentException("usage: login <username> <password>");
        }
        RegisterRequest req = new RegisterRequest(args[1], args[2], args[3]);
        RegisterResult res = facade.registerUser(req);
        this.authToken = res.authToken();
        postLoginRepl();
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
