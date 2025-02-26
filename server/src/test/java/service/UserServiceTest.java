package service;

import chess.data.UserData;
import org.junit.jupiter.api.*;
import dataaccess.*;
import service.request.LoginRequest;
import service.request.RegisterRequest;
import service.result.LoginResult;
import service.result.RegisterResult;


public class UserServiceTest {

    private UserDataAccessor userDataAccessor;
    private AuthDataAccessor authDataAccessor;

    @BeforeEach
    public void initializeTests() {
        userDataAccessor = new MemoryUserDataAccessor();
        authDataAccessor = new MemoryAuthDataAccessor();
    }

    @Test
    @DisplayName("Register positive")
    public void registerSuccessful() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("Bob", "shizbuckets", "derp@loler.com");
        RegisterResult observed = UserService.registerService(request, userDataAccessor, authDataAccessor);
        RegisterResult expected = new RegisterResult("Bob", observed.authToken());
        Assertions.assertEquals(expected, observed);
    }

    @Test
    @DisplayName("Register 400 Error")
    public void register400() {
        RegisterRequest request = new RegisterRequest("Bob", "", "derp@loler.com");
        Assertions.assertThrows(InsufficientParametersException.class, () -> UserService.registerService(request, userDataAccessor, authDataAccessor));
    }

    @Test
    @DisplayName("Register 403 Error")
    public void register403() {
        RegisterRequest request = new RegisterRequest("Bob", "shizbuckets", "derp@loler.com");
        userDataAccessor.createUser(new UserData("Bob", "frick", "crud@derp.com"));
        Assertions.assertThrows(InvalidParametersException.class, () -> UserService.registerService(request, userDataAccessor, authDataAccessor));
    }

    @Test
    @DisplayName("Login Success")
    public void loginSuccessful() throws InvalidParametersException {
        initializeRegistry();
        LoginRequest loginRequest = new LoginRequest("Bob", "shizbuckets");
        LoginResult observed = UserService.loginService(loginRequest, userDataAccessor, authDataAccessor);
        LoginResult expected = new LoginResult("Bob", observed.authToken());
        Assertions.assertEquals(observed, expected);
    }

    @Test
    @DisplayName("Login Unauthorized")
    public void login401() {
        initializeRegistry();
        LoginRequest loginRequest = new LoginRequest("Bob", "wrong password");
        Assertions.assertThrows(InvalidParametersException.class, () -> UserService.loginService(loginRequest, userDataAccessor, authDataAccessor));
    }

    private void initializeRegistry() {
        userDataAccessor.createUser(new UserData("Bob", "shizbuckets", "derp@loler.com"));
    }
}
