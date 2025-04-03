package server;

import com.google.gson.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.*;
import service.GamePlayService;
import websocket.Deserializer;
import websocket.commands.*;
import websocket.messages.ServerMessage;

import java.lang.reflect.Type;

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
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> playService.connect(command, session);
            case MAKE_MOVE -> throw new RuntimeException("NOT IMPLEMENTED");
            case LEAVE -> throw new RuntimeException("NOT IMPLEMENTED");
            case RESIGN -> throw new RuntimeException("NOT IMPLEMENTED");
        }
    }
}
