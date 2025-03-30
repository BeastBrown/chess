package websocket.messages;

public class NotificationMessage extends ServerMessage {

    private String message;

    public NotificationMessage(ServerMessageType type, String nMessage) {
        super(type);
        this.message = nMessage;
    }

    public String getMessage() {
        return message;
    }
}
