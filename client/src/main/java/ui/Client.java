package ui;

import javax.sound.midi.SysexMessage;
import java.util.Scanner;

public class Client {

    private String serverUrl;
    private Scanner scanner;

    public Client(String url) {
        serverUrl = url;
        scanner = new Scanner(System.in);
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
            registerUser();
        } else if (input.startsWith("login")) {
            loginUser();
        } else if (!input.equals("quit")) {
            printPreLoginHelp();
        }
    }

    private void loginUser() {
    }

    private void registerUser() {
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
