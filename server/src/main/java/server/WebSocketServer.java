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

    public WebSocketServer(GamePlayService gamePlayService) {
        this.playService = gamePlayService;
        this.gson = Deserializer.getGson();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        Logger.getGlobal().log(Level.FINE, "Entering the on message thing");
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> playService.connect(command, session);
            case MAKE_MOVE -> throw new RuntimeException("NOT IMPLEMENTED");
            case LEAVE -> playService.leave(command, session);
            case RESIGN -> throw new RuntimeException("NOT IMPLEMENTED");
        }
    }
}
