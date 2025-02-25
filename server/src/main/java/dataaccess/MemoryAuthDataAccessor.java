package dataaccess;

import chess.data.AuthData;
import java.util.HashMap;

public class MemoryAuthDataAccessor implements AuthDataAccessor {

    private HashMap<String, AuthData> authMap;

    public MemoryAuthDataAccessor() {
        authMap = new HashMap<String, AuthData>();
    }

    @Override
    public AuthData getAuth(String authToken) {
        return authMap.get(authToken);
    }

    @Override
    public void createAuth(AuthData authData) {
        authMap.put(authData.authToken(), authData);
    }

    @Override
    public void deleteAuth(String authToken) {
        authMap.remove(authToken);
    }
}
