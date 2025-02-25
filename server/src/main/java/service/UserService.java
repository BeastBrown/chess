package service;

import chess.data.AuthData;
import chess.data.UserData;
import dataaccess.AuthDataAccessor;
import dataaccess.InsufficientParametersException;
import dataaccess.InvalidParametersException;
import dataaccess.UserDataAccessor;
import service.request.RegisterRequest;
import service.result.RegisterResult;

import java.util.Objects;
import java.util.UUID;

public class UserService {
    public static RegisterResult registerService(RegisterRequest registerRequest,
                                                 UserDataAccessor userAccessor,
                                                 AuthDataAccessor authAccessor) throws
            InsufficientParametersException,
            InvalidParametersException {

        if (necessaryFieldsEmpty(registerRequest)) {
            throw new InsufficientParametersException("username and password cannot be empty");
        }
        if (usernameTaken(registerRequest, userAccessor)) {
            throw new InvalidParametersException("username is taken");
        }
        UserData userData = new UserData(registerRequest.username(),
                registerRequest.password(), registerRequest.email());
        userAccessor.createUser(userData);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, registerRequest.username());
        authAccessor.createAuth(authData);
        return new RegisterResult(authData.username(), authData.authToken());
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
}
