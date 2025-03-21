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

    public LoginResult loginUser(LoginRequest req) throws IOException {
        HashMap<String, String> props = new HashMap<String, String>();
        String resBody = comm.doRequest("/session", "POST", props, gson.toJson(req));
        return gson.fromJson(resBody, LoginResult.class);
    }

    public LogoutResult logoutUser(LogoutRequest req) throws IOException {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("authorization", req.authToken());
        String resBody = comm.doRequest("/session", "DELETE", props, "");
        return gson.fromJson(resBody, LogoutResult.class);
    }

    public ListGameResult listGames(ListGameRequest req) throws IOException {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("authorization", req.authToken());
        String resBody = comm.doRequest("/game", "GET", props, "");
        return gson.fromJson(resBody, ListGameResult.class);
    }

    public JoinGameResult joinGame(JoinGameRequest req) throws IOException {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("authorization", req.authToken());
        DesiredGame reqBodyObj = new DesiredGame(req.playerColor(), req.gameID());
        String resBody = comm.doRequest("/game", "PUT", props, gson.toJson(reqBodyObj));
        return gson.fromJson(resBody, JoinGameResult.class);
    }

    public CreateGameResult createGame(CreateGameRequest req) throws IOException {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("authorization", req.authToken());
        GameName reqBodyObj = new GameName(req.gameName());
        String resBody = comm.doRequest("/game", "POST", props, gson.toJson(reqBodyObj));
        return gson.fromJson(resBody, CreateGameResult.class);
    }

}
