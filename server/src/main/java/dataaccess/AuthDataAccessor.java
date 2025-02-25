package dataaccess;

import chess.data.AuthData;

public interface AuthDataAccessor {
    public AuthData getAuth(String authToken);
    public void createAuth(AuthData authData);
    public void deleteAuth(String authToken);
}
