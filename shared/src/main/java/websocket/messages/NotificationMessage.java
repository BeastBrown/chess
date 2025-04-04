package websocket.messages;

public class NotificationMessage extends ServerMessage {

    private String message;

    public NotificationMessage(String nMessage) {
        super(ServerMessageType.NOTIFICATION);
        this.message = nMessage;
    }

    public String getMessage() {
        return message;
    }
}
