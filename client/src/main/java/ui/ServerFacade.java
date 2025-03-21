package ui;

import chess.request.RegisterRequest;
import chess.result.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;

public class ServerFacade {

    ClientCommunicator comm;
    Gson gson;

    public ServerFacade(String url) {
        comm = new ClientCommunicator(url);
        gson = new Gson();
    }

    public RegisterResult registerUser(RegisterRequest req) throws IOException {
        HashMap<String, String> props = new HashMap<String, String>();
        String resBody = comm.doRequest("/session", "POST", props, gson.toJson(req));
        return gson.fromJson(resBody, RegisterResult.class);
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
