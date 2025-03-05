package dataaccess;

import chess.data.UserData;

public class MySqlUserDataAccessor implements UserDataAccessor {

    @Override
    public void clear() {
    }

    @Override
    public void createUser(UserData userdata) {

    }

    @Override
    public UserData getUser(String username) {
        return null;
    }

    private void createTable() {
        ;
    }
}
