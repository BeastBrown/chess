package service;

import chess.*;
import chess.data.AuthData;
import chess.data.GameData;
import com.google.gson.Gson;
import dataaccess.AuthDataAccessor;
import dataaccess.GameDataAccessor;
import dataaccess.InvalidParametersException;
import dataaccess.UserDataAccessor;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import websocket.Deserializer;
import websocket.commands.MoveCommand;
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

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;
import static websocket.messages.ServerMessage.ServerMessageType.*;

public class GamePlayService {

    private UserService userService;
    private UserDataAccessor userAccessor;
    private GameDataAccessor gameAccessor;
    private AuthDataAccessor authAccessor;
    private HashMap<Integer, ArrayList<Session>> gameMap;
    private Gson gson;
    private static Logger logger = Logger.getGlobal();

    public GamePlayService(UserService userService, UserDataAccessor userAccessor,
                           GameDataAccessor gameAccessor, AuthDataAccessor authAccessor) {
        this.userService = userService;
        this.userAccessor = userAccessor;
        this.gameAccessor = gameAccessor;
        this.authAccessor = authAccessor;
        this.gameMap = new HashMap<Integer, ArrayList<Session>>();
        this.gson = Deserializer.getGson();
    }

    public void makeMove(MoveCommand command, Session session) {
        Integer id = command.getGameID();
        GameData gameData = gameAccessor.getGame(id);
        ChessMove move = command.getMove();
        String username = getUsername(command);
        ServerMessage moverMessage = getMoverMessage(command, gameData, move);
        sendMoveMessages(moverMessage, move, session, gameData, username);
    }

    private void sendMoveMessages(ServerMessage moveMessage, ChessMove move,
                                  Session session, GameData gameData, String username) {
        if (moveMessage.getServerMessageType().equals(ERROR)) {
            sendMessage(session, moveMessage);
            return;
        }
        sendAllMessage(gameData.gameID(), moveMessage,null);
        NotificationMessage moveNotification = getMoveNotification(move, username, gameData);
        sendAllMessage(gameData.gameID(), moveNotification, session);
        sendUpdatedState(gameData, username);
    }

    private void sendUpdatedState(GameData gameData, String username) {
        GameData newData = gameAccessor.getGame(gameData.gameID());
        ChessGame newGame = newData.game();
        ChessGame.TeamColor opposingAllegiance = getOpposingAllegiance(gameData, username);
        String message = getMessageString(newGame, newData, opposingAllegiance);
        NotificationMessage stateNotification = new NotificationMessage(message);
        if (message != null) {
            sendAllMessage(newData.gameID(), stateNotification, null);
        }
    }

    private static ChessGame.TeamColor getOpposingAllegiance(GameData gameData, String username) {
        return getAllegiance(gameData, username).equals("White") ?
                BLACK : WHITE;
    }

    private String getMessageString(ChessGame game, GameData newData,
                                    ChessGame.TeamColor opposingAllegiance) {
        boolean inCheck = game.isInCheck(opposingAllegiance);
        boolean inCheckmate = game.isInCheckmate(opposingAllegiance);
        boolean inStalemate = game.isInStalemate(opposingAllegiance);
        String opposingUsername = getUsernameColor(opposingAllegiance, newData);
        String message = null;
        if (inCheckmate) {
            message = opposingUsername + " as " + opposingAllegiance.toString() + " is in Checkmate";
            setGameOver(newData);
        } else if (inCheck) {
            message = opposingUsername + " as " + opposingAllegiance.toString() + " is in Check";
        } else if (inStalemate) {
            message = "The game has ended in a stalemate. GOOD JOB";
            setGameOver(newData);
        }
        return message;
    }

    private String getUsernameColor(ChessGame.TeamColor opposingAllegiance, GameData newData) {
        return opposingAllegiance == WHITE ? newData.whiteUsername() : newData.blackUsername();
    }

    private NotificationMessage getMoveNotification(ChessMove move,  String username,
                                                    GameData gameData) {
        String allegiance = getAllegiance(gameData, username);
        String message = username + " as " + allegiance + " has moved from " +
                getFormattedMove(move);
        return new NotificationMessage(message);
    }

    private String getFormattedMove(ChessMove move) {
        ChessPosition firstPos = move.getStartPosition();
        ChessPosition secondPos = move.getEndPosition();
        String firstPosStr = getColLetter(firstPos.getColumn()) +
                String.valueOf(firstPos.getRow());
        String secondPosStr = getColLetter(secondPos.getColumn()) +
                String.valueOf(secondPos.getRow());
        return firstPosStr + " to " + secondPosStr;
    }

    private String getColLetter(int column) {
        String[] columns = {"A", "B", "C", "D", "E", "F", "G", "H"};
        return columns[column - 1];
    }

    private ServerMessage getMoverMessage(MoveCommand command,
                                          GameData gameData, ChessMove move) {
        try {
            validateBasicFields(command, gameData);
            validateIsPlayer(command, gameData);
            validateIsPlayerPiece(command, move, gameData);
            validateIsPlayerTurn(command, gameData);
            validateIsActive(gameData);
            gameData.game().makeMove(move);
        } catch (InvalidParametersException | InvalidMoveException e) {
            return new ErrorMessage(e.getMessage());
        } catch (Exception e) {
            return new ErrorMessage("Move could not be executed due to " + e.getMessage());
        }
        gameAccessor.updateGameData(gameData);
        return new LoadGameMessage(gameData.game());
    }

    private void validateIsPlayerPiece(MoveCommand command, ChessMove move, GameData gameData) throws InvalidParametersException {
        ChessPosition from = move.getStartPosition();
        String username = getUsername(command);
        String allegiance = getAllegiance(gameData, username);
        ChessGame.TeamColor side = allegiance.equals("White") ? WHITE : BLACK;
        ChessPiece piece = gameData.game().getBoard().getPiece(from);
        if (piece == null) {
            throw new InvalidParametersException("There is no piece on that square");
        }
        if (!piece.getTeamColor().equals(side)) {
            throw new InvalidParametersException("You cant move somebody else's piece");
        }
    }

    private void validateIsPlayerTurn(MoveCommand command, GameData gameData)
            throws InvalidParametersException {
        String username = getUsername(command);
        String allegiance = getAllegiance(gameData, username);
        ChessGame.TeamColor turn =  gameData.game().getTeamTurn();
        ChessGame.TeamColor playerAllegiance = allegiance.equals("White") ? WHITE : BLACK;
        if (!turn.equals(playerAllegiance)) {
            throw new InvalidParametersException("Its not your turn yet");
        }
    }

    private void validateIsActive(GameData gameData) throws InvalidParametersException {
        if (!gameData.game().isActive) {
            throw new InvalidParametersException("The Game Is already over");
        }
    }

    public void resign(UserGameCommand command, Session session) {
        Integer id = command.getGameID();
        GameData gameData = gameAccessor.getGame(id);
        String username = getUsername(command);
        ServerMessage resignerMessage = getResignerMessage(command, gameData, session);
        ServerMessage resignAllMessage = getResignAllMessage(username, resignerMessage);
        sendMessage(session, resignerMessage);
        if (resignAllMessage != null) {
            sendAllMessage(id, resignAllMessage, session);
        }
    }

    private ServerMessage getResignAllMessage(String username, ServerMessage resignerMessage) {
        return resignerMessage.getServerMessageType().equals(ERROR) ? null :
                new NotificationMessage(username + " Has Resigned the match like a QUITTER!");
    }

    private ServerMessage getResignerMessage(UserGameCommand command, GameData gameData, Session session) {
        try {
            validateBasicFields(command, gameData);
            validateIsPlayer(command, gameData);
            validateIsActive(gameData);
        } catch (InvalidParametersException e) {
            return new ErrorMessage(e.getMessage());
        }
        setGameOver(gameData);
        return new NotificationMessage("You have successfully resigned, QUITTER!");
    }

    private void validateIsPlayer(UserGameCommand command, GameData gameData) throws InvalidParametersException {
        String username = getUsername(command);
        String allegiance = getAllegiance(gameData, username);
        if (allegiance.equals("Observer")) {
            throw new InvalidParametersException("You have to play a player to do this");
        }
    }

    private void setGameOver(GameData gameData) {
        gameData.game().isActive = false;
        gameAccessor.updateGameData(gameData);
    }

    public void leave(UserGameCommand command, Session session) {
        Integer id = command.getGameID();
        GameData gameData = gameAccessor.getGame(id);
        ServerMessage leaverMessage = getLeaverMessage(command, gameData, session);
        ServerMessage allMessage = getLeaveAllMessage(leaverMessage, gameData, command);
        sendAllMessage(id, allMessage, session);
    }

    private ServerMessage getLeaveAllMessage(ServerMessage leaverMessage,
                                             GameData gameData, UserGameCommand command) {
        String username = getUsername(command);
        if (leaverMessage.getServerMessageType().equals(ERROR)) {
            return new NotificationMessage(username +
                    " Tried to leave but we wouldn't let him");
        }
        return new NotificationMessage(username + " has left the game");
    }

    private ServerMessage getLeaverMessage(UserGameCommand command, GameData gameData,
                                           Session session) {
        try {
            validateBasicFields(command, gameData);
        } catch (InvalidParametersException e) {
            return new ErrorMessage(e.getMessage());
        }
        gameMap.get(gameData.gameID()).remove(session);
        String username = getUsername(command);
        String allegiance = getAllegiance(gameData, username);
        if (!allegiance.equals("Observer")) {
            removePlayer(gameData, allegiance);
        }
        return new NotificationMessage("You have left the game");
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
        } else {
            gameMap.get(id).add(session);
        }
        sendMessage(session, cMessage);
        if (!allMessage.getServerMessageType().equals(ERROR)) {
            sendAllMessage(id, allMessage, session);
        }
    }

    private void sendAllMessage(Integer id, ServerMessage allMessage, Session not) {
        ArrayList<Session> sList = getSessions(id);
        if (sList == null) {
            logger.log(Level.INFO, "session list was null");
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

    private void initGameEntry(Session session, Integer id) {
        ArrayList<Session> firstSesList = new ArrayList<Session>();
        firstSesList.add(session);
        gameMap.put(id, firstSesList);
    }

    private ServerMessage getConnectAllMessage(ServerMessage cMessage,
                                               UserGameCommand command, GameData gameData) {
        if (cMessage.getServerMessageType().equals(ERROR)) {
            return new ErrorMessage("Somebody tried to connect but failed");
        }
        String username = getUsername(command);
        String allegiance = getAllegiance(gameData, username);
        return new NotificationMessage(username + " has joined as " + allegiance);
    }

    private String getUsername(UserGameCommand command) {
        AuthData auth = authAccessor.getAuth(command.getAuthToken());
        return auth != null ? auth.username() : null;

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
            return new ErrorMessage(e.getMessage());
        }
        return new LoadGameMessage(gameData.game());
    }

    private void sendMessage(Session s, ServerMessage message) {
        logger.log(Level.INFO, "entering the server message sender");
        String sMessage = gson.toJson(message);
        boolean isSessionOpen = s.isOpen();
        if (!isSessionOpen) {
            logger.log(Level.INFO, "The Session was not open");
            return;
        }
        try {
            s.getRemote().sendString(sMessage);
            logger.log(Level.INFO, "The message was successfully sent to id " + sMessage);
        } catch (IOException | WebSocketException e) {
            logger.log(Level.SEVERE, "we couldn't send this message | " + sMessage);
        }
    }

    public void clear() {
        this.gameMap.clear();
    }
}
