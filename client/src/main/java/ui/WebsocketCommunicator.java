package ui;

import com.google.gson.Gson;
import websocket.Deserializer;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ClientEndpoint
public class WebsocketCommunicator extends Endpoint {

    private URI uri;
    private ServerMessageObserver observer;
    private Session session;
    private static Gson gson = Deserializer.getGson();
    private static Logger logger = Logger.getGlobal();

    public WebsocketCommunicator(String uri, ServerMessageObserver observer) {
        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "The URI had bad syntax");
            throw new RuntimeException(e);
        }
        this.observer = observer;
    }

    public void establishConnection() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(this, this.uri);
        } catch (DeploymentException | IOException e) {
            logger.log(Level.SEVERE, "We could not establish the connection for some reason");
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        logger.log(Level.INFO, "Entering the message sender");
        if (!session.isOpen()) {
            logger.log(Level.SEVERE, "The Session is not open");
            return;
        }
        try {
            session.getBasicRemote().sendText(message);
            logger.log(Level.INFO, "message sent | " + message);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "We got an exception while sending the message");
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        session.addMessageHandler(new WsHandler());
        this.session = session;
        logger.log(Level.INFO, "WS connection established, message handler registered with session ID " + session.getId());
    }

    public void disconnect() {
        try {
            this.session.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not close the connection for some reason");
            throw new RuntimeException(e);
        }
        this.session = null;
    }

    public class WsHandler implements MessageHandler.Whole<String> {
        @Override
        public void onMessage(String jsonString) {
            logger.log(Level.INFO, "received this server message " + jsonString);
            ServerMessage message = gson.fromJson(jsonString, ServerMessage.class);
            observer.notify(message);
        }
    }

}
