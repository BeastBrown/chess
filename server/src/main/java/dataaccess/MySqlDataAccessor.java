package dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlDataAccessor {

    private static boolean isDbInitialized = false;

    public MySqlDataAccessor() throws DataAccessException {
        if (!isDbInitialized) {
            DatabaseManager.createDatabase();
            DatabaseManager.initializeTables();

        }
        isDbInitialized = true;
    }

    protected void executeParameterizedUpdate(String statement, String[] values) throws DataAccessException {
        try(Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement prepStatement = setArguments(statement, values, conn);
            prepStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    protected ResultSet executeParameterizedQuery(String statement, String[] values) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {;
            PreparedStatement pStatement = setArguments(statement, values, conn);
            return pStatement.executeQuery();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private static PreparedStatement setArguments(String statement, String[] values, Connection conn) throws SQLException {
        PreparedStatement pStatement = conn.prepareStatement(statement);
        for(int i = 1; i < values.length+1 ; i++) {
            pStatement.setString(i, values[i-1]);
        }
        return pStatement;
    }
}
