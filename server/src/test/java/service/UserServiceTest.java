package service;

import chess.data.UserData;
import org.junit.jupiter.api.*;
import dataaccess.*;
import service.request.LoginRequest;
import service.request.LogoutRequest;
import service.request.RegisterRequest;
import service.result.LoginResult;
import service.result.LogoutResult;
import service.result.RegisterResult;


public class UserServiceTest {

    private UserDataAccessor userDataAccessor;
    private AuthDataAccessor authDataAccessor;
    private UserService userService;

    @BeforeEach
    public void initializeTests() throws DataAccessException {
        userDataAccessor = new MySqlUserDataAccessor();
        authDataAccessor = new MySqlAuthDataAccessor();

        userDataAccessor.clear();
        authDataAccessor.clear();

        userService = new UserService(userDataAccessor, authDataAccessor);
    }

    @Test
    @DisplayName("Register positive")
    public void registerSuccessful() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("Bob", "shizbuckets", "derp@loler.com");
        RegisterResult observed = userService.registerService(request);
        RegisterResult expected = new RegisterResult("Bob", observed.authToken());
        Assertions.assertEquals(expected, observed);
    }

    @Test
    @DisplayName("Register 400 Error")
    public void register400() {
        RegisterRequest request = new RegisterRequest("Bob", "", "derp@loler.com");
        Assertions.assertThrows(InsufficientParametersException.class, () -> userService.registerService(request));
    }

    @Test
    @DisplayName("Register 403 Error")
    public void register403() {
        RegisterRequest request = new RegisterRequest("Bob", "shizbuckets", "derp@loler.com");
        userDataAccessor.createUser(new UserData("Bob", "frick", "crud@derp.com"));
        Assertions.assertThrows(InvalidParametersException.class, () -> userService.registerService(request));
    }

    @Test
    @DisplayName("Login Success")
    public void loginSuccessful() throws InvalidParametersException, InsufficientParametersException {
        initializeRegistry();
        LoginRequest loginRequest = new LoginRequest("Bob", "shizbuckets");
        LoginResult observed = userService.loginService(loginRequest);
        LoginResult expected = new LoginResult("Bob", observed.authToken());
        Assertions.assertEquals(observed, expected);
    }

    @Test
    @DisplayName("Login Unauthorized")
    public void login401() throws InvalidParametersException, InsufficientParametersException {
        initializeRegistry();
        LoginRequest loginRequest = new LoginRequest("Bob", "wrong password");
        Assertions.assertThrows(InvalidParametersException.class, () -> userService.loginService(loginRequest));
    }

    private void initializeRegistry() throws InvalidParametersException, InsufficientParametersException {
        RegisterRequest request = new RegisterRequest("Bob", "shizbuckets", "derp@loler.com");
        userService.registerService(request);
    }

    @Test
    @DisplayName("Logout Successful")
    public void logoutSuccessful() throws InvalidParametersException, InsufficientParametersException {
        initializeRegistry();
        LoginRequest loginRequest = new LoginRequest("Bob", "shizbuckets");
        LoginResult loginResult = userService.loginService(loginRequest);
        LogoutRequest logoutRequest = new LogoutRequest(loginResult.authToken());
        LogoutResult observed = userService.logoutService(logoutRequest);
        LogoutResult expected = new LogoutResult();
        Assertions.assertEquals(observed, expected);
    }

    @Test
    @DisplayName("Logout Unauthorized")
    public void logout401() throws InvalidParametersException, InsufficientParametersException {
        initializeRegistry();
        LoginRequest loginRequest = new LoginRequest("Bob", "shizbuckets");
        LoginResult loginResult = userService.loginService(loginRequest);
        LogoutRequest logoutRequest = new LogoutRequest("very low chance that this is the auth");
        Assertions.assertThrows(InvalidParametersException.class,() -> userService.logoutService(logoutRequest));
    }
}
