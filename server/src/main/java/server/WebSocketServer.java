package server;

import com.google.gson.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.*;
import service.GamePlayService;
import websocket.commands.*;
import websocket.messages.ServerMessage;

import java.lang.reflect.Type;

@WebSocket
public class WebSocketServer {

    private GamePlayService playService;
    private Gson gson;

    public WebSocketServer(GamePlayService gamePlayService) {
        this.playService = gamePlayService;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(UserGameCommand.class, new CommandDeserializer())
                .create();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand command = getCommand(message);
        switch (command.getCommandType()) {
            case CONNECT -> playService.connect(command, session);
            case MAKE_MOVE -> throw new RuntimeException("NOT IMPLEMENTED");
            case LEAVE -> throw new RuntimeException("NOT IMPLEMENTED");
            case RESIGN -> throw new RuntimeException("NOT IMPLEMENTED");
        }
    }

    private UserGameCommand getCommand(String jsonString) {
        return gson.fromJson(jsonString, UserGameCommand.class);
    }

    public static class CommandDeserializer implements JsonDeserializer<UserGameCommand> {

        @Override
        public UserGameCommand deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObj =  jsonElement.getAsJsonObject();
            String cType = jsonObj.get("commandType").getAsString();
            return cType.equals("MAKE_MOVE") ?
                    jsonDeserializationContext.deserialize(jsonElement, MoveCommand.class) :
                    jsonDeserializationContext.deserialize(jsonElement, UserGameCommand.class);
        }
    }
}
