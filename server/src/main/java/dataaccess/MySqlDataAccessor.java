package dataaccess;


import com.mysql.cj.x.protobuf.MysqlxPrepare;

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
                CREATE TABLE users (
                username varchar(255) NOT NULL,
                password varchar(255) NOT NULL,
                email varchar(255) NOT NULL ,
                PRIMARY KEY(username));
                """;
        String createGames = """
                CREATE TABLE games (
                id int AUTO INCREMENT,
                whiteUsername varchar(255),
                blackUsername varchar(255),
                gameName varchar(255),
                game varchar(255),
                PRIMARY KEY(id));
                """;
        String createAuths = """
                CREATE TABLE auths (
                authToken varchar(255),
                username varchar(255),
                PRIMARY KEY(username));
                """;
        try(Connection conn = DatabaseManager.getConnection()) {
            executeModification(conn, createUsers);
            executeModification(conn, createGames);
            executeModification(conn , createAuths);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private void executeModification(Connection conn, String statement) throws DataAccessException {
        try {
            PreparedStatement prepStatement = conn.prepareStatement(statement);
            prepStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
