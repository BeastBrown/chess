package ui;

import chess.request.RegisterRequest;
import chess.result.*;

public class ServerFacade {

    ClientCommunicator comm;

    public ServerFacade(String url) {
        comm = new ClientCommunicator(url);
    }

    public RegisterResult registerUser(RegisterRequest req) {
        throw new RuntimeException("not implemented");
    }

    public LoginResult loginUser() {
        throw new RuntimeException("not implemented");
    }

    public LogoutResult logoutUser() {
        throw new RuntimeException("not implemented");
    }

    public ListGameResult listGames() {
        throw new RuntimeException("not implemented");
    }

    public JoinGameResult joinGame() {
        throw new RuntimeException("not implemented");
    }

    public CreateGameResult createGame() {
        throw new RuntimeException("not implemented");
    }


}
