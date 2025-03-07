package dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySqlDataAccessor {

    private static boolean isDbInitialized = false;

    public MySqlDataAccessor() throws DataAccessException {
        if (!isDbInitialized) {
            DatabaseManager.createDatabase();
            initializeTables();

        }
        isDbInitialized = true;
    }

    private void initializeTables() throws DataAccessException {
        String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                username varchar(255) NOT NULL,
                password varchar(255) NOT NULL,
                email varchar(255) NOT NULL ,
                PRIMARY KEY(username));
                """;
        String createGames = """
                CREATE TABLE IF NOT EXISTS games (
                id int AUTO INCREMENT,
                whiteUsername varchar(255),
                blackUsername varchar(255),
                gameName varchar(255),
                game varchar(255),
                PRIMARY KEY(id));
                """;
        String createAuths = """
                CREATE TABLE IF NOT EXISTS auths (
                authToken varchar(255),
                username varchar(255),
                PRIMARY KEY(username));
                """;
        try(Connection conn = DatabaseManager.getConnection()) {
            executeModification(createUsers);
            executeModification(createGames);
            executeModification(createAuths);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    protected void executeModification(String statement) throws DataAccessException {
        Connection conn = null;
        try(Connection c = DatabaseManager.getConnection()) {
            conn = c;
            PreparedStatement prepStatement = conn.prepareStatement(statement);
            prepStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
