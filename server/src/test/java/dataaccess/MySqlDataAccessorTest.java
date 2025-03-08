package dataaccess;

import chess.data.UserData;
import org.junit.jupiter.api.*;

public class MySqlDataAccessorTest {

    private UserDataAccessor userAccessor;

    @BeforeEach
    public void initializeServer() throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.initializeTables();
        userAccessor = new MySqlUserDataAccessor();
        userAccessor.clear();
    }

    @Test
    @DisplayName("clear positive")
    public void clearSuccess() {
        UserData toInsert = new UserData("bob", "shizbuckets", "derp@loler.com");
        userAccessor.createUser(toInsert);
        userAccessor.clear();
        UserData observed = userAccessor.getUser("bob");
        Assertions.assertNull(observed);
    }

    @Test
    @DisplayName("insert and get user positive")
    public void insertGetSuccess() {
        UserData toInsert = new UserData("bob", "shizbuckets", "derp@loler.com");
        userAccessor.createUser(toInsert);
        UserData observed = userAccessor.getUser("bob");
        Assertions.assertEquals(toInsert, observed);
    }
}
