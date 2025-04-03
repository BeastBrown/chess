package websocket;

import com.google.gson.*;
import websocket.commands.MoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.lang.reflect.Type;

public class Deserializer {

    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(UserGameCommand.class, new UserCommandDeserializer())
            .registerTypeAdapter(ServerMessage.class, new ServerMessageDeserializer())
            .create();

    public static Gson getGson() {
        return gson;
    }

    private static class UserCommandDeserializer implements JsonDeserializer<UserGameCommand> {

        @Override
        public UserGameCommand deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObj =  jsonElement.getAsJsonObject();
            String cType = jsonObj.get("commandType").getAsString();
            return cType.equals("MAKE_MOVE") ?
                    jsonDeserializationContext.deserialize(jsonElement, MoveCommand.class) :
                    new Gson().fromJson(jsonElement, UserGameCommand.class);
        }
    }

    private static class ServerMessageDeserializer implements JsonDeserializer<ServerMessage> {

        @Override
        public ServerMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObj =  jsonElement.getAsJsonObject();
            String mType = jsonObj.get("commandType").getAsString();
            return switch(mType) {
                case "ERROR" -> jsonDeserializationContext.deserialize(jsonElement, ErrorMessage.class);
                case "LOAD_GAME" -> jsonDeserializationContext.deserialize(jsonElement, LoadGameMessage.class);
                default -> jsonDeserializationContext.deserialize(jsonElement, NotificationMessage.class);
            };
        }
    }
}
