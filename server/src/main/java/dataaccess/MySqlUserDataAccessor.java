package dataaccess;

import chess.data.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlUserDataAccessor extends MySqlDataAccessor implements UserDataAccessor {


    public MySqlUserDataAccessor() throws DataAccessException {
    }

    @Override
    public void clear() {
        String clearAuths = """
                DELETE FROM auths;
                """;
        try {
            executeModification(clearAuths);
        } catch (DataAccessException e) {
            throw new RuntimeException("Clearing the auths failed");
        }
    }

    @Override
    public void createUser(UserData userdata) {
        String insertAuth = """
                INSERT INTO users (username, password, email) VALUES (?, ?, ?);
                """;
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement insertStatement = conn.prepareStatement(insertAuth);
            insertStatement.setString(1, userdata.username());
            insertStatement.setString(2, userdata.password());
            insertStatement.setString(3, userdata.email());
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Create user statement failed");
        }
    }

    @Override
    public UserData getUser(String username) {
        String accessUser = """
                SELECT username, password, email FROM users WHERE username = ?;
                """;
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement grabUser = conn.prepareStatement(accessUser);
            grabUser.setString(1, username);
            ResultSet rs =  grabUser.executeQuery();
            rs.next();
            UserData userData = new UserData(rs.getString("username"),
                    rs.getString("password"), rs.getString("email"));
            return userData;
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Get user statement failed");
        }
    }
}
