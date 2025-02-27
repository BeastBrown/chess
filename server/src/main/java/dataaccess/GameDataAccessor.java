package dataaccess;

import chess.ChessGame;
import chess.data.GameData;

import java.util.Collection;

public interface GameDataAccessor {
    public GameData getGame(Integer gameID);
    public Collection<GameData> listGames();
    public void createGame(GameData gameData);
    public Integer getCounterID();
    public void updateGameData(GameData gameData);
    public void clear();
}
