package service;

import dataaccess.GameDataAccessor;
import dataaccess.UserDataAccessor;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;
import static websocket.messages.ServerMessage.ServerMessageType.ERROR;

public class GamePlayService {

    private UserService userService;
    private UserDataAccessor userAccessor;
    private GameDataAccessor gameAccessor;

    public GamePlayService(UserService userService, UserDataAccessor userAccessor, GameDataAccessor gameAccessor) {
        this.userService = userService;
        this.userAccessor = userAccessor;
        this.gameAccessor = gameAccessor;
    }

    public ServerMessage connect(UserGameCommand command, Session session) {
        boolean isLoggedIn = userService.isAuthenticated(command.getAuthToken());
        if (!isLoggedIn) {
             return new ErrorMessage(ERROR, "User not authenticated");
        }
        throw new RuntimeException("NOT IMPLEMENTED")
    }
}
