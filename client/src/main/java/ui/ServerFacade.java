package ui;

import chess.ChessMove;
import chess.request.*;
import chess.result.*;
import com.google.gson.Gson;
import websocket.Deserializer;
import websocket.commands.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import static websocket.commands.UserGameCommand.CommandType.*;

public class ServerFacade {

    private HttpCommunicator httpComm;
    private WebsocketCommunicator wsComm;
    private Gson gson;

    public ServerFacade(String url, ServerMessageObserver observer) {
        httpComm = new HttpCommunicator(url);
        wsComm = new WebsocketCommunicator(url, observer);
        gson = Deserializer.getGson();
    }

    public void clearDatabase() throws IOException {
        httpComm.doRequest("/db", "DELETE", new HashMap<>(), "");
    }

    public RegisterResult registerUser(RegisterRequest req) throws IOException {
        HashMap<String, String> props = new HashMap<String, String>();
        try {
            String resBody = httpComm.doRequest("/user", "POST", props, gson.toJson(req));
            return gson.fromJson(resBody, RegisterResult.class);
        } catch (IOException e) {
            String code = e.getMessage();
            String newMessage = code.equals("403") ? "The Username Is Already Taken" :
                    "You shouldn't be seeing this, its a code " + code;
            throw new IOException(newMessage);
        }
    }

    public LoginResult loginUser(LoginRequest req) throws IOException {
        HashMap<String, String> props = new HashMap<String, String>();
        try {
            String resBody = httpComm.doRequest("/session", "POST", props, gson.toJson(req));
            return gson.fromJson(resBody, LoginResult.class);
        } catch (IOException e) {
            String code = e.getMessage();
            String newMessage = code.equals("401") ? "Invalid Credentials" :
                    "You shouldn't be seeing this, its a code " + code;
            throw new IOException(newMessage);
        }
    }

    public LogoutResult logoutUser(LogoutRequest req) throws IOException {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("authorization", req.authToken());
        try {
            String resBody = httpComm.doRequest("/session", "DELETE", props, "");
            return gson.fromJson(resBody, LogoutResult.class);
        } catch (IOException e) {
            String code = e.getMessage();
            String newMessage = code.equals("401") ? "You weren't logged in in the first place!" :
                    "You shouldn't be seeing this, its a code " + code;
            throw new IOException(newMessage);
        }
    }

    public ListGameResult listGames(ListGameRequest req) throws IOException {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("authorization", req.authToken());
        try {
            String resBody = httpComm.doRequest("/game", "GET", props, "");
            return gson.fromJson(resBody, ListGameResult.class);
        } catch (IOException e) {
            String code = e.getMessage();
            String newMessage = code.equals("401") ? "You aren't Logged In!" :
                    "You shouldn't be seeing this, its a code " + code;
            throw new IOException(newMessage);
        }
    }

    public JoinGameResult joinGame(JoinGameRequest req) throws IOException {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("authorization", req.authToken());
        DesiredGame reqBodyObj = new DesiredGame(req.playerColor(), req.gameID());
        try {
            String resBody = httpComm.doRequest("/game", "PUT", props, gson.toJson(reqBodyObj));
            return gson.fromJson(resBody, JoinGameResult.class);
        } catch (IOException e) {
            String code = e.getMessage();
            String newMessage = switch(code) {
                case "401" -> "You're not logged in";
                case "403" -> "Somebody already took that color";
                default -> "You shouldn't be seeing this, its a code " + code;
            };
            throw new IOException(newMessage);
        }
    }

    public CreateGameResult createGame(CreateGameRequest req) throws IOException {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("authorization", req.authToken());
        GameName reqBodyObj = new GameName(req.gameName());
        try {
            String resBody = httpComm.doRequest("/game", "POST", props, gson.toJson(reqBodyObj));
            return gson.fromJson(resBody, CreateGameResult.class);
        } catch (IOException e) {
            String code = e.getMessage();
            String newMessage = code.equals("401") ? "You aren't Logged In!" :
                    "You shouldn't be seeing this, its a code " + code;
            throw new IOException(newMessage);
        }
    }

    public void connectToWebsocket(String authToken, Integer gameID) {
        UserGameCommand command = new UserGameCommand(CONNECT, authToken, gameID);
        wsComm.establishConnection();
        wsComm.sendMessage(gson.toJson(command));
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move) {
        MoveCommand command = new MoveCommand(authToken, gameID, move);
        wsComm.sendMessage(gson.toJson(command));
    }

    public void resign(String authToken, Integer gameID) {
        UserGameCommand command = new UserGameCommand(RESIGN, authToken, gameID);
        wsComm.sendMessage(gson.toJson(command));
    }

    public void leave(String authToken, Integer gameID) {
        UserGameCommand command = new UserGameCommand(LEAVE, authToken, gameID);
        wsComm.sendMessage(gson.toJson(command));
        wsComm.disconnect();
    }
}
