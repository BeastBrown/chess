package server;

import com.google.gson.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.*;
import service.GamePlayService;
import websocket.Deserializer;
import websocket.commands.*;
import websocket.messages.ServerMessage;

import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebSocket
public class WebSocketServer {

    private GamePlayService playService;
    private Gson gson;
    private static final Logger logger = Logger.getGlobal();

    public WebSocketServer(GamePlayService gamePlayService) {
        this.playService = gamePlayService;
        this.gson = Deserializer.getGson();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        logger.log(Level.FINE, "Entering the on message thing");
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> playService.connect(command, session);
            case MAKE_MOVE -> playService.makeMove((MoveCommand) command, session);
            case LEAVE -> playService.leave(command, session);
            case RESIGN -> playService.resign(command, session);
        }
    }
}
