package dataaccess;

import chess.data.AuthData;

public class MySqlAuthDataAccessor extends MySqlDataAccessor implements AuthDataAccessor {

    public MySqlAuthDataAccessor() throws DataAccessException {
    }

    @Override
    public AuthData getAuth(String authToken) {
        return null;
    }

    @Override
    public void createAuth(AuthData authData) {

    }

    @Override
    public void deleteAuth(String authToken) {

    }

    @Override
    public void clear() {

    }
}
