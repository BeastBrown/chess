package dataaccess;

import chess.data.AuthData;

import java.util.ArrayList;
import java.util.HashMap;

public class MySqlAuthDataAccessor extends MySqlDataAccessor implements AuthDataAccessor {

    public MySqlAuthDataAccessor() throws DataAccessException {
    }

    @Override
    public AuthData getAuth(String authToken) {
        String authAccess = """
                SELECT username FROM auths WHERE authToken = ?;
                """;
        String[] arguments = new String[1];
        arguments[0] = authToken;
        try {
            ArrayList<HashMap<String, Object>> resultList =
                    executeParameterizedQuery(authAccess, arguments);
            HashMap<String, Object> resultMap = resultList.isEmpty() ? null : resultList.getFirst();
            return resultMap == null ? null :
                    new AuthData(authToken, (String) resultMap.get("username"));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createAuth(AuthData authData) {
        String authCreate = """
                INSERT INTO auths (authToken, username) VALUES (?, ?);
                """;
//        ON DUPLICATE KEY UPDATE authToken = VALUES(authToken)
        String[] arguments = new String[2];
        arguments[0] = authData.authToken();
        arguments[1] = authData.username();
        try {
            executeParameterizedUpdate(authCreate, arguments);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAuth(String authToken) {
        String authDelete = """
                DELETE FROM auths WHERE authToken = ?;
                """;
        String[] arguments = new String[1];
        arguments[0] = authToken;
        try {
            executeParameterizedUpdate(authDelete, arguments);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void clear() {
        String clearAuths = """
                TRUNCATE TABLE auths;
                """;
        try {
            executeParameterizedUpdate(clearAuths, new String[0]);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
