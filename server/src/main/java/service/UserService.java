package service;

import chess.data.AuthData;
import chess.data.UserData;
import dataaccess.AuthDataAccessor;
import dataaccess.UserDataAccessor;
import service.request.RegisterRequest;
import service.result.RegisterResult;

import java.util.UUID;

public class UserService {
    public static RegisterResult registerService(RegisterRequest registerRequest,
                                                 UserDataAccessor userAccessor,
                                                 AuthDataAccessor authAccessor) {
        UserData userData = new UserData(registerRequest.username(),
                registerRequest.password(), registerRequest.email());
        userAccessor.createUser(userData);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, registerRequest.username());
        authAccessor.createAuth(authData);
        return new RegisterResult(authData.username(), authData.authToken());
    }
}
