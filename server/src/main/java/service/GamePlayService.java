package service;

import chess.data.GameData;
import com.google.gson.Gson;
import dataaccess.AuthDataAccessor;
import dataaccess.GameDataAccessor;
import dataaccess.UserDataAccessor;
import org.eclipse.jetty.websocket.api.Session;
import websocket.Deserializer;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

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
        this.gson = Deserializer.getGson();
    }

    public void connect(UserGameCommand command, Session session) {
        Integer id = command.getGameID();
        GameData gameData = gameAccessor.getGame(id);
        ServerMessage cMessage = getConnectorMessage(command, gameData);
        ServerMessage allMessage = getConnectAllMessage(cMessage, command, gameData);
        ArrayList<Session> sList = gameMap.get(id);
        if (sList == null) {
            initGameEntry(session, id);
        }
        sendMessage(session, cMessage);
        sendAllMessage(id, allMessage, session);
    }

    private void sendAllMessage(Integer id, ServerMessage allMessage, Session not) {
        for (Session s : gameMap.get(id)) {
            if (!s.equals(not)) {
                sendMessage(s, allMessage);
            }
        }
    }

    private void initGameEntry(Session session, Integer ID) {
        ArrayList<Session> firstSesList = new ArrayList<Session>();
        firstSesList.add(session);
        gameMap.put(ID, firstSesList);
    }

    private ServerMessage getConnectAllMessage(ServerMessage cMessage,
                                               UserGameCommand command, GameData gameData) {
        String username = authAccessor.getAuth(command.getAuthToken()).username();
        if (cMessage.getServerMessageType().equals(ERROR)) {
            return new NotificationMessage(NOTIFICATION ,
                    username + "tried to connect but failed");
        }
        String allegiance = getAllegiance(gameData, username);
        return new NotificationMessage(NOTIFICATION, username + " has joined as " + allegiance);
    }

    private static String getAllegiance(GameData gameData, String username) {
        String allegiance;
        if (username.equals(gameData.whiteUsername())) {
            allegiance = "White";
        } else if (username.equals(gameData.blackUsername())) {
            allegiance = "Black";
        } else {
            allegiance = "Observer";
        }
        return allegiance;
    }

    private ServerMessage getConnectorMessage(UserGameCommand command, GameData gameData) {
        boolean isLoggedIn = userService.isAuthenticated(command.getAuthToken());
        if (!isLoggedIn) {
            return new ErrorMessage(ERROR, "User not authenticated");
        }
        if (gameData == null) {
            return new ErrorMessage(ERROR, "Game not found");
        }
        return new LoadGameMessage(LOAD_GAME, gameData.game());
    }

    private void sendMessage(Session s, ServerMessage message) {
        String sMessage = gson.toJson(message);
        try {
            s.getRemote().sendString(sMessage);
        } catch (IOException e) {
            System.out.println("we couldn't send this message | " + sMessage);
        }
    }
}
