package service;

import chess.data.AuthData;
import chess.data.UserData;
import dataaccess.AuthDataAccessor;
import dataaccess.InsufficientParametersException;
import dataaccess.InvalidParametersException;
import dataaccess.UserDataAccessor;
import org.mindrot.jbcrypt.BCrypt;
import chess.request.LoginRequest;
import chess.request.LogoutRequest;
import chess.request.RegisterRequest;
import chess.result.LogoutResult;
import chess.result.RegisterResult;
import chess.result.LoginResult;

import java.util.Objects;
import java.util.UUID;

public class UserService {

    private UserDataAccessor userAccessor;
    private AuthDataAccessor authAccessor;

    public UserService(UserDataAccessor userAccessor, AuthDataAccessor authAccessor) {
        this.userAccessor = userAccessor;
        this.authAccessor = authAccessor;
    }

    public RegisterResult registerService(RegisterRequest registerRequest) throws
            InsufficientParametersException,
            InvalidParametersException {

        validateRegisterFields(registerRequest);
        String hashedPassword = BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt());
        UserData userData = new UserData(registerRequest.username(),
                hashedPassword, registerRequest.email());
        userAccessor.createUser(userData);

        LoginRequest loginRequest = new LoginRequest(registerRequest.username(), registerRequest.password());
        AuthData authData = executeLogin(loginRequest);
        return new RegisterResult(authData.username(), authData.authToken());
    }

    private AuthData executeLogin(LoginRequest loginRequest) {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, loginRequest.username());
        authAccessor.createAuth(authData);
        return authData;
    }

    private void validateRegisterFields(RegisterRequest registerRequest) throws InsufficientParametersException, InvalidParametersException {
        if (necessaryFieldsEmpty(registerRequest)) {
            throw new InsufficientParametersException("username and password cannot be empty");
        }
        if (usernameTaken(registerRequest)) {
            throw new InvalidParametersException("username is taken");
        }
    }

    private boolean usernameTaken(RegisterRequest registerRequest) {
        return userAccessor.getUser(registerRequest.username()) != null;
    }

    private static boolean necessaryFieldsEmpty(RegisterRequest registerRequest) {
        return Objects.isNull(registerRequest.username()) || registerRequest.username().isEmpty() ||
                Objects.isNull(registerRequest.password()) || registerRequest.password().isEmpty() ||
                Objects.isNull(registerRequest.email()) || registerRequest.email().isEmpty();
    }

    public LoginResult loginService(LoginRequest loginRequest) throws
            InvalidParametersException {
        UserData user = userAccessor.getUser(loginRequest.username());
        if (user == null || !BCrypt.checkpw(loginRequest.password(), user.password())) {
            throw new InvalidParametersException("Username doesn't exist, or password is invalid");
        }
        AuthData authData = executeLogin(loginRequest);
        return new LoginResult(authData.username(), authData.authToken());
    }

    public LogoutResult logoutService(LogoutRequest logoutRequest) throws InvalidParametersException {
        AuthData authData = authAccessor.getAuth(logoutRequest.authToken());
        if (authData == null) {
            throw new InvalidParametersException("Invalid AuthToken");
        }
        authAccessor.deleteAuth(authData.authToken());
        return new LogoutResult();
    }

    public boolean isAuthenticated(String authToken) {
        return authAccessor.getAuth(authToken) != null;
    }
}
