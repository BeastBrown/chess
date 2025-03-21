package ui;

import chess.request.*;
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

    public void clearDatabase() throws IOException {
        comm.doRequest("/db", "DELETE", new HashMap<>(), "");
    }

    public RegisterResult registerUser(RegisterRequest req) throws IOException {
        HashMap<String, String> props = new HashMap<String, String>();
        String resBody = comm.doRequest("/user", "POST", props, gson.toJson(req));
        return gson.fromJson(resBody, RegisterResult.class);
    }

    public LoginResult loginUser(LoginRequest req) {
        throw new RuntimeException("not implemented");
    }

    public LogoutResult logoutUser(LogoutRequest req) {
        throw new RuntimeException("not implemented");
    }

    public ListGameResult listGames(ListGameRequest req) {
        throw new RuntimeException("not implemented");
    }

    public JoinGameResult joinGame(JoinGameRequest req) {
        throw new RuntimeException("not implemented");
    }

    public CreateGameResult createGame(CreateGameRequest req) {
        throw new RuntimeException("not implemented");
    }

}
