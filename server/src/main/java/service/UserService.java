package service;

import chess.data.AuthData;
import chess.data.UserData;
import dataaccess.AuthDataAccessor;
import dataaccess.InsufficientParametersException;
import dataaccess.InvalidParametersException;
import dataaccess.UserDataAccessor;
import service.request.LoginRequest;
import service.request.RegisterRequest;
import service.result.RegisterResult;
import service.result.LoginResult;

import java.util.Objects;
import java.util.UUID;

public class UserService {
    public static RegisterResult registerService(RegisterRequest registerRequest,
                                                 UserDataAccessor userAccessor,
                                                 AuthDataAccessor authAccessor) throws
            InsufficientParametersException,
            InvalidParametersException {

        validateRegisterFields(registerRequest, userAccessor);
        UserData userData = new UserData(registerRequest.username(),
                registerRequest.password(), registerRequest.email());
        userAccessor.createUser(userData);

        LoginRequest loginRequest = new LoginRequest(registerRequest.username(), registerRequest.password());
        AuthData authData = executeLogin(loginRequest, authAccessor);
        return new RegisterResult(authData.username(), authData.authToken());
    }

    private static AuthData executeLogin(LoginRequest loginRequest, AuthDataAccessor authAccessor) {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, loginRequest.username());
        authAccessor.createAuth(authData);
        return authData;
    }

    private static void validateRegisterFields(RegisterRequest registerRequest, UserDataAccessor userAccessor) throws InsufficientParametersException, InvalidParametersException {
        if (necessaryFieldsEmpty(registerRequest)) {
            throw new InsufficientParametersException("username and password cannot be empty");
        }
        if (usernameTaken(registerRequest, userAccessor)) {
            throw new InvalidParametersException("username is taken");
        }
    }

    private static boolean usernameTaken(RegisterRequest registerRequest,
                                         UserDataAccessor userAccessor) {
        return userAccessor.getUser(registerRequest.username()) != null;
    }

    private static boolean necessaryFieldsEmpty(RegisterRequest registerRequest) {
        return Objects.equals(registerRequest.username(), "") ||
                Objects.equals(registerRequest.password(), "") ||
                Objects.equals(registerRequest.email(), "");
    }

    public static LoginResult loginService(LoginRequest loginRequest,
                               UserDataAccessor userAccessor, AuthDataAccessor authAccessor) throws
            InvalidParametersException {
        UserData user = userAccessor.getUser(loginRequest.username());
        if (user == null || !user.password().equals(loginRequest.password())) {
            throw new InvalidParametersException("Username doesn't exist, or password is invalid");
        }
        AuthData authData = executeLogin(loginRequest, authAccessor);
        return new LoginResult(authData.username(), authData.authToken());
    }
}
