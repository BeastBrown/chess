package dataaccess;

import chess.data.UserData;
import java.util.HashMap;

public class MemoryUserDataAccessor implements UserDataAccessor {

    private HashMap<String, UserData> userMap;

    public MemoryUserDataAccessor() {
        this.userMap = new HashMap<String, UserData>();
    }

    @Override
    public void clear() {
        userMap.clear();
    }

    @Override
    public void createUser(UserData userData) {
        userMap.put(userData.username(), userData);
    }

    @Override
    public UserData getUser(String username) {
        return userMap.get(username);
    }
}
