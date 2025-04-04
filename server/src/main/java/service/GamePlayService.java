package service;

import chess.data.GameData;
import com.google.gson.Gson;
import dataaccess.AuthDataAccessor;
import dataaccess.GameDataAccessor;
import dataaccess.InvalidParametersException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public void leave(UserGameCommand command, Session session) {
        Integer id = command.getGameID();
        GameData gameData = gameAccessor.getGame(id);
        ServerMessage leaverMessage = getLeaverMessage(command, gameData, session);
        ServerMessage allMessage = getLeaveAllMessage(leaverMessage, gameData, command);
        sendMessage(session, leaverMessage);
        sendAllMessage(id, allMessage, session);
    }

    private ServerMessage getLeaveAllMessage(ServerMessage leaverMessage,
                                             GameData gameData, UserGameCommand command) {
        String username = getUsername(command);
        if (leaverMessage.getServerMessageType().equals(ERROR)) {
            return new NotificationMessage(NOTIFICATION, username +
                    " Tried to leave but we wouldn't let him");
        }
        return new NotificationMessage(NOTIFICATION, username + " has left the game");
    }

    private ServerMessage getLeaverMessage(UserGameCommand command, GameData gameData,
                                           Session session) {
        try {
            validateBasicFields(command, gameData);
        } catch (InvalidParametersException e) {
            return new ErrorMessage(ERROR, e.getMessage());
        }
        gameMap.get(gameData.gameID()).remove(session);
        String username = getUsername(command);
        String allegiance = getAllegiance(gameData, username);
        if (!allegiance.equals("Observer")) {
            removePlayer(gameData, allegiance);
        }
        return new NotificationMessage(NOTIFICATION, "You have left the game");
    }

    private void removePlayer(GameData gameData, String allegiance) {
        String newWhite = allegiance.equals("White") ? null : gameData.whiteUsername();
        String newBlack = allegiance.equals("Black") ? null : gameData.blackUsername();
        GameData newGame = new GameData(gameData.gameID(), newWhite,
                newBlack, gameData.gameName(), gameData.game());
        gameAccessor.updateGameData(newGame);
    }

    private void validateBasicFields(UserGameCommand command, GameData gameData)
            throws InvalidParametersException {
        boolean isLoggedIn = userService.isAuthenticated(command.getAuthToken());
        if (!isLoggedIn) {
            throw new InvalidParametersException("User not authenticated");
        }
        if (gameData == null) {
            throw new InvalidParametersException("Game not found");
        }
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
        if (allMessage.getServerMessageType().equals(ERROR)) {
            sendAllMessage(id, allMessage, session);
        }
    }

    private void sendAllMessage(Integer id, ServerMessage allMessage, Session not) {
        ArrayList<Session> sList = getSessions(id);
        if (sList == null) {
            Logger.getGlobal().log(Level.FINE, "session list was null");
            return;
        }
        for (Session s : sList) {
            if (!s.equals(not)) {
                sendMessage(s, allMessage);
            }
        }
    }

    private ArrayList<Session> getSessions(Integer id) {
        return gameMap.get(id);
    }

    private void initGameEntry(Session session, Integer ID) {
        ArrayList<Session> firstSesList = new ArrayList<Session>();
        firstSesList.add(session);
        gameMap.put(ID, firstSesList);
    }

    private ServerMessage getConnectAllMessage(ServerMessage cMessage,
                                               UserGameCommand command, GameData gameData) {
        String username = getUsername(command);
        if (cMessage.getServerMessageType().equals(ERROR)) {
            return new NotificationMessage(NOTIFICATION ,
                    username + " tried to connect but failed");
        }
        String allegiance = getAllegiance(gameData, username);
        return new NotificationMessage(NOTIFICATION, username + " has joined as " + allegiance);
    }

    private String getUsername(UserGameCommand command) {
        return authAccessor.getAuth(command.getAuthToken()).username();
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
        try {
            validateBasicFields(command, gameData);
        } catch (InvalidParametersException e) {
            return new ErrorMessage(ERROR, e.getMessage());
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
