package dataaccess;

import chess.data.UserData;

public interface UserDataAccessor {

    public void clear();

    public void createUser(UserData userdata);

    public UserData getUser(String username) throws DataAccessException;
}
