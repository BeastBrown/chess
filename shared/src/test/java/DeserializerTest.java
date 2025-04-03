import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import websocket.Deserializer;
import websocket.commands.MoveCommand;
import websocket.commands.UserGameCommand;

public class DeserializerTest {

    private static Gson gson = Deserializer.getGson();

    @Test
    @DisplayName("Move command")
    public void  moveTest() {
        MoveCommand expected = new MoveCommand("cool auth", 1,
                new ChessMove(new ChessPosition(1, 1),
                        new ChessPosition(1,2), null));
        String toPass = gson.toJson(expected);
        UserGameCommand observed = gson.fromJson(toPass, UserGameCommand.class);
        Assertions.assertEquals(expected, observed);
    }

    @Test
    @DisplayName("Connect Command")
    public void connectGood() {
        String toPass = "{\"commandType\":\"CONNECT\",\"authToken\":\"aa34e43a-5b1a-4498-a7d9-d1e8349bc5d4\",\"gameID\":1}";
        UserGameCommand expected = new UserGameCommand(UserGameCommand.CommandType.CONNECT,
                "aa34e43a-5b1a-4498-a7d9-d1e8349bc5d4", 1);
        String expectedLiteral = gson.toJson(expected);
        UserGameCommand observed = gson.fromJson(toPass, UserGameCommand.class);

        Assertions.assertEquals(expected, observed);
        Assertions.assertEquals(expectedLiteral, toPass);
    }
}
