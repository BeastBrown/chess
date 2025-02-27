package service;

import dataaccess.AuthDataAccessor;
import dataaccess.GameDataAccessor;
import dataaccess.UserDataAccessor;
import service.result.ClearResult;

public class ClearService {

    private UserDataAccessor userAccessor;
    private AuthDataAccessor authAccessor;
    private GameDataAccessor gameAccessor;

    public ClearService(UserDataAccessor user, AuthDataAccessor auth, GameDataAccessor game) {
        userAccessor = user;
        authAccessor = auth;
        gameAccessor = game;
    }

    ClearResult clear() {
        userAccessor.clear();
        authAccessor.clear();
        gameAccessor.clear();
        return new ClearResult();
    }
}
