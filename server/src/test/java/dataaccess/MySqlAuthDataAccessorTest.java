package dataaccess;

import chess.data.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MySqlAuthDataAccessorTest {

    private AuthDataAccessor authAccessor;

    @BeforeEach
    public void initializeServer() throws DataAccessException {
        authAccessor = new MySqlAuthDataAccessor();
        authAccessor.clear();
    }

    @Test
    @DisplayName("Clear positive")
    public void clearSuccessful() {
        AuthData toAdd = new AuthData("cool auth", "bob");
        authAccessor.createAuth(toAdd);
        authAccessor.clear();
        AuthData observed = authAccessor.getAuth("cool auth");
        Assertions.assertNull(observed);
    }

    @Test
    @DisplayName("Create Auth Positive")
    public void createAuthSuccessful() {
        AuthData toAdd = new AuthData("cool auth", "bob");
        authAccessor.createAuth(toAdd);
        AuthData observed = authAccessor.getAuth("cool auth");
        Assertions.assertEquals(toAdd, observed);
    }

    @Test
    @DisplayName("Create Auth Negative")
    public void createAuthNullFailure() {
        Assertions.assertThrows(NullPointerException.class, () -> authAccessor.createAuth(null));
    }

    @Test
    @DisplayName("Get Auth Positive")
    public void getAuthSuccessful() {
        AuthData toAdd = new AuthData("cool auth", "bob");
        authAccessor.createAuth(toAdd);
        AuthData observed = authAccessor.getAuth("cool auth");
        Assertions.assertEquals(toAdd, observed);
    }

    @Test
    @DisplayName("Get Auth Negative")
    public void getAuthNullFailure() {
        AuthData observed = authAccessor.getAuth("not this auth");
        Assertions.assertNull(observed);
    }

    @Test
    @DisplayName("Delete Auth Positive")
    public void deleteAuthSuccessful() {
        AuthData toAdd = new AuthData("cool auth", "bob");
        authAccessor.createAuth(toAdd);
        authAccessor.deleteAuth("cool auth");
        AuthData observed = authAccessor.getAuth("cool auth");
        Assertions.assertNull(observed);
    }

    @Test
    @DisplayName("Delete Auth Negative")
    public void deleteAuthFailureNothingHappens() {
        AuthData toAdd = new AuthData("cool auth", "bob");
        authAccessor.createAuth(toAdd);
        authAccessor.deleteAuth("fake auth");
        AuthData observed = authAccessor.getAuth("cool auth");
        Assertions.assertEquals(toAdd, observed);
    }

}
