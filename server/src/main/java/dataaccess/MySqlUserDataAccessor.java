package dataaccess;

import chess.data.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class MySqlUserDataAccessor extends MySqlDataAccessor implements UserDataAccessor {


    public MySqlUserDataAccessor() throws DataAccessException {
    }

    @Override
    public void clear() {
        String clearUsers = """
                TRUNCATE TABLE users;
                """;
        try {
            executeParameterizedUpdate(clearUsers, new String[0]);
        } catch (DataAccessException e) {
            throw new RuntimeException("Clearing the users failed");
        }
    }

    @Override
    public void createUser(UserData userdata) {
        String insertAuth = """
                INSERT INTO users (username, password, email) VALUES (?, ?, ?);
                """;
        String[] userArguments = new String[3];
        userArguments[0] = userdata.username();
        userArguments[1] = userdata.password();
        userArguments[2] = userdata.email();
        try {
            executeParameterizedUpdate(insertAuth, userArguments);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserData getUser(String username) {
        String accessUser = """
                SELECT username, password, email FROM users WHERE username = ?;
                """;
        String[] queryArguments = new String[1];
        queryArguments[0] = username;
        try {
            ArrayList<HashMap<String, Object>> resultList =
                    executeParameterizedQuery(accessUser, queryArguments);
            HashMap<String, Object> userRow = resultList.isEmpty() ? null : resultList.getFirst();
            return userRow == null ? null :
                    new UserData((String) userRow.get("username"),
                            (String) userRow.get("password"), (String) userRow.get("email"));

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
