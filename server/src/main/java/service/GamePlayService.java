package service;

import chess.ChessGame;
import chess.data.GameData;
import com.google.gson.Gson;
import dataaccess.AuthDataAccessor;
import dataaccess.GameDataAccessor;
import dataaccess.UserDataAccessor;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.management.Notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static websocket.messages.ServerMessage.ServerMessageType.*;

public class GamePlayService {

    private UserService userService;
    private UserDataAccessor userAccessor;
    private GameDataAccessor gameAccessor;
    private AuthDataAccessor authAccessor;
    private HashMap<Integer, ArrayList<Session>> gameMap;
    private Gson gson;

    public GamePlayService(UserService userService, UserDataAccessor userAccessor,
                           GameDataAccessor gameAccessor, AuthDataAccessor authAccessor) {
        this.userService = userService;
        this.userAccessor = userAccessor;
        this.gameAccessor = gameAccessor;
        this.authAccessor = authAccessor;
        this.gameMap = new HashMap<Integer, ArrayList<Session>>();
        this.gson = new Gson();
    }

    public void connect(UserGameCommand command, Session session) {
        boolean isLoggedIn = userService.isAuthenticated(command.getAuthToken());
        if (!isLoggedIn) {
             return new ErrorMessage(ERROR, "User not authenticated");
        }
        Integer ID = command.getGameID();
        GameData game = gameAccessor.getGame(ID);
        if (game == null) {
            return new ErrorMessage(ERROR, "Game not found");
        }
        String username = authAccessor.getAuth(command.getAuthToken()).username();
        String allegiance;
        if (username.equals(game.whiteUsername())) {
            allegiance = "White";
        } else if (username.equals(game.blackUsername())) {
            allegiance = "Black";
        } else {
            allegiance = "Observer";
        }
        String notification = username + " connected as " + allegiance;
        NotificationMessage nMsg = new NotificationMessage(NOTIFICATION, notification);
        ArrayList<Session> sList = gameMap.get(ID);
        if (sList == null) {
            ArrayList<Session> firstSesList = new ArrayList<Session>();
            firstSesList.add(session);
            gameMap.put(ID, firstSesList);
        }
        for (Session s : gameMap.get(ID)) {
            if (!s.equals(session)) {
                try {
                    s.getRemote().sendString(gson.toJson(nMsg));
                } catch (IOException e) {
                    System.out.println("we couldn't send this message | " + gson.toJson(nMsg));
                }
            }
        }
        LoadGameMessage lMsg = new LoadGameMessage(LOAD_GAME, game.game());
        try {
            session.getRemote().sendString(gson.toJson(lMsg));
        } catch (IOException e) {
            System.out.println("we couldn't send this message | " + gson.toJson(lMsg));
        }
    }
}
