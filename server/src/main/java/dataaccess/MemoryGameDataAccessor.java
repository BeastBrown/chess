package dataaccess;

import chess.ChessGame;
import chess.data.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MemoryGameDataAccessor implements GameDataAccessor {

    private HashMap<Integer, GameData> gameDataMap;
    private Integer counterID;

    public MemoryGameDataAccessor() {
        gameDataMap = new HashMap<Integer, GameData>();
        counterID = 1;
    }

    @Override
    public GameData getGame(Integer gameID) {
        return gameDataMap.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() {
        return gameDataMap.values();
    }

    @Override
    public void createGame(GameData gameData) {
        gameDataMap.put(counterID, gameData);
        counterID++;
    }

    @Override
    public Integer getCounterID() {
        return counterID;
    }

    @Override
    public void updateGameData(GameData gameData) {
        gameDataMap.put(gameData.gameID(), gameData);
    }

    @Override
    public void clear() {
        gameDataMap.clear();
        counterID = 1;
    }
}
