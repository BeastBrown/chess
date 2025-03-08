package dataaccess;

import chess.ChessGame;
import chess.data.GameData;
import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MySqlGameDataAccessor extends MySqlDataAccessor implements GameDataAccessor {

    private Gson gson;

    public MySqlGameDataAccessor() throws DataAccessException {
        gson = new Gson();
    }

    @Override
    public GameData getGame(Integer gameID) {
        String gameAccess = """
                SELECT id, whiteUsername, blackUsername, gameName, game FROM games WHERE id = ?;
                """;
        Integer[] arguments = new Integer[1];
        arguments[0] = gameID;
        try {
            ArrayList<HashMap<String, Object>> resultList =  executeParameterizedQuery(gameAccess, arguments);
            return resultList.isEmpty() ? null : constructGame(resultList.getFirst());
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private GameData constructGame(HashMap<String, Object> dataMap) {
        ChessGame realGame = gson.fromJson((String) dataMap.get("game"), ChessGame.class);
        return new GameData((Integer) dataMap.get("id"), (String) dataMap.get("whiteUsername"),
                (String) dataMap.get("blackUsername"), (String) dataMap.get("gameName"), realGame);
    }

    @Override
    public Collection<GameData> listGames() {
        String allGames = """
                SELECT id, whiteUsername, blackUsername, gameName, game FROM games;
                """;
        try {
            ArrayList<GameData> gameList = new ArrayList<GameData>();
            ArrayList<HashMap<String, Object>> resultList = executeParameterizedQuery(allGames, new Object[0]);
            for (HashMap<String, Object> rowMap : resultList) {
                gameList.add(constructGame(rowMap));
            }
            return gameList;
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createGame(GameData gameData) {
        String makeGame = """
                INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?);
                """;
        String[] arguments = new String[4];
        arguments[0] = gameData.whiteUsername();
        arguments[1] = gameData.blackUsername();
        arguments[2] = gameData.gameName();
        arguments[3] = gson.toJson(gameData.game());
        try {
            executeParameterizedUpdate(makeGame, arguments);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Integer getCounterID() {
        String highestID = """
                SELECT id FROM games ORDER BY id DESC LIMIT 1;
                """;
        try {
            ArrayList<HashMap<String, Object>> resultList = executeParameterizedQuery(highestID, new Object[0]);
            int highest = resultList.isEmpty() ? 0 : (Integer) resultList.getFirst().get("id");
            return highest + 1;

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateGameData(GameData gameData) {
        String changeGame = """
                UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ? WHERE id = ?;
                """;
        Object[] arguments = new Object[4];
        arguments[0] = gameData.whiteUsername();
        arguments[1] = gameData.blackUsername();
        arguments[2] = gameData.gameName();
        arguments[3] = gameData.gameID();
        try {
            executeParameterizedUpdate(changeGame, arguments);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void clear() {
        String clearGames = """
                TRUNCATE TABLE games;
                """;
        try {
            executeParameterizedUpdate(clearGames, new Object[0]);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
