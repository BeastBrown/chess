package dataaccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MySqlDataAccessor {

    private static boolean isDbInitialized = false;

    public MySqlDataAccessor() throws DataAccessException {
        if (!isDbInitialized) {
            DatabaseManager.createDatabase();
            DatabaseManager.initializeTables();

        }
        isDbInitialized = true;
    }

    protected void executeParameterizedUpdate(String statement, Object[] values) throws DataAccessException {
        try(Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement prepStatement = setArguments(statement, values, conn);
            prepStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    protected ArrayList<HashMap<String, Object>> executeParameterizedQuery(String statement, Object[] values) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement pStatement = setArguments(statement, values, conn);
            ResultSet rs =  pStatement.executeQuery();

            return getRsInMemory(rs);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private static ArrayList<HashMap<String, Object>> getRsInMemory(ResultSet rs) throws SQLException {
        ArrayList<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
        ResultSetMetaData metaData =  rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while(rs.next()) {
            HashMap<String, Object> rowMap = new HashMap<String, Object>();
            for (int i = 1; i < columnCount+1; i++) {
                String cName = metaData.getColumnName(i);
                rowMap.put(cName, rs.getString(cName));
            }
            resultList.add(rowMap);
        }
        return resultList;
    }

    private static PreparedStatement setArguments(String statement, Object[] values, Connection conn) throws SQLException {
        PreparedStatement pStatement = conn.prepareStatement(statement);
        for(int i = 1; i < values.length+1 ; i++) {
            pStatement.setObject(i, values[i-1]);
        }
        return pStatement;
    }
}
