package dataaccess;

import chess.data.GameData;

import java.util.Collection;
import java.util.List;

public class MySqlGameDataAccessor extends MySqlDataAccessor implements GameDataAccessor {
    public MySqlGameDataAccessor() throws DataAccessException {
    }

    @Override
    public GameData getGame(Integer gameID) {
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        return List.of();
    }

    @Override
    public void createGame(GameData gameData) {

    }

    @Override
    public Integer getCounterID() {
        return 0;
    }

    @Override
    public void updateGameData(GameData gameData) {

    }

    @Override
    public void clear() {

    }
}
