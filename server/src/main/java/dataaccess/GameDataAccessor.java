package dataaccess;

import chess.ChessGame;
import chess.data.GameData;

import java.util.Collection;

public interface GameDataAccessor {
    public GameData getGame(Integer gameID);
    public Collection<GameData> listGames();
    public void createGame(GameData gameData);
    /**
     * Returns what should be the GameID for the next
     * game in the Database.
     * This is Semi-Deprecated in the SQL implementation,
     * I implemented it in the SQL just so the Memory and
     * SQL version can both work
     * */
    public Integer getCounterID();
    public void updateGameData(GameData gameData);
    public void clear();
}
