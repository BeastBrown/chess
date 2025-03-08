package dataaccess;

import chess.ChessGame;
import chess.data.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

public class MySqlGameDataAccessorTest {

    GameDataAccessor gameAccessor;

    @BeforeEach
    public void initializeServer() throws DataAccessException {
        gameAccessor = new MySqlGameDataAccessor();
        gameAccessor.clear();
    }

    @Test
    @DisplayName("Get Game Positive")
    public void getGameSuccessful() {
        GameData toAdd = new GameData(1, null, null, "Best Game", new ChessGame());
        gameAccessor.createGame(toAdd);
        GameData observed = gameAccessor.getGame(1);
        Assertions.assertEquals(toAdd, observed);
    }

    @Test
    @DisplayName("Get Game Negative")
    public void getGameNullFailure() {
        GameData toAdd = new GameData(1, null, null, "Best Game", new ChessGame());
        gameAccessor.createGame(toAdd);
        GameData observed = gameAccessor.getGame(2);
        Assertions.assertNull(observed);
    }

    @Test
    @DisplayName("Get Counter ID positive")
    public void getCounterIdSuccessful() {
        GameData toAdd = new GameData(1, null, null, "Best Game", new ChessGame());
        gameAccessor.createGame(toAdd);
        int observed = gameAccessor.getCounterID();
        Assertions.assertEquals(2, observed);
    }

    @Test
    @DisplayName("Clear Positive")
    public void clearSuccessful() {
        GameData toAdd = new GameData(1, null, null, "Best Game", new ChessGame());
        gameAccessor.createGame(toAdd);
        gameAccessor.clear();
        GameData observed = gameAccessor.getGame(1);
        Assertions.assertNull(observed);
    }

    @Test
    @DisplayName("Create Game Positive")
    public void createGameSuccessful() {
        GameData toAdd = new GameData(1, null, null, "Best Game", new ChessGame());
        gameAccessor.createGame(toAdd);
        GameData observed = gameAccessor.getGame(1);
        Assertions.assertEquals(toAdd, observed);
    }

    @Test
    @DisplayName("Create Game Negative")
    public void createGameFailure() {
        GameData toAdd = null;
        Assertions.assertThrows(NullPointerException.class, () -> gameAccessor.createGame(toAdd));
    }

    @Test
    @DisplayName("Display Game Positive")
    public void displayGameSuccessful() {
        GameData toAdd = new GameData(1, null, null, "Best Game", new ChessGame());
        gameAccessor.createGame(toAdd);
        GameData toAdd2 = new GameData(2, null, null, "Worst Game", new ChessGame());
        gameAccessor.createGame(toAdd2);

        Collection<GameData> observed = gameAccessor.listGames();
        Collection<GameData> expected = new ArrayList<>();
        expected.add(toAdd);
        expected.add(toAdd2);
        Assertions.assertEquals(expected, observed);
    }

    @Test
    @DisplayName("Display Game Negative")
    public void displayGameNoContent() {
        Collection<GameData> observed = gameAccessor.listGames();
        Assertions.assertTrue(observed.isEmpty());
    }

    @Test
    @DisplayName("Update Game Positive")
    public void updateGameSuccessful() {
        GameData toAdd = new GameData(1, null, null, "Best Game", new ChessGame());
        gameAccessor.createGame(toAdd);
        GameData modified = new GameData(1, "bob", null, "Best Game", new ChessGame());
        gameAccessor.updateGameData(modified);
        GameData observed = gameAccessor.getGame(1);
        Assertions.assertEquals(modified, observed);
    }

    @Test
    @DisplayName("Update Game Negative")
    public void updateNonExistentGameNoUpdate() {
        GameData toAdd = new GameData(1, null, null, "Best Game", new ChessGame());
        gameAccessor.createGame(toAdd);
        GameData modified = new GameData(2, "bob", null, "Best Game", new ChessGame());
        gameAccessor.updateGameData(modified);
        GameData observed = gameAccessor.getGame(1);
        Assertions.assertEquals(toAdd, observed);
    }
}
