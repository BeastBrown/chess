package service;

import dataaccess.AuthDataAccessor;
import dataaccess.GameDataAccessor;
import dataaccess.UserDataAccessor;
import chess.result.ClearResult;

public class ClearService {

    private UserDataAccessor userAccessor;
    private AuthDataAccessor authAccessor;
    private GameDataAccessor gameAccessor;
    private GamePlayService playService;

    public ClearService(UserDataAccessor user, AuthDataAccessor auth,
                        GameDataAccessor game, GamePlayService gamePlay) {
        userAccessor = user;
        authAccessor = auth;
        gameAccessor = game;
        playService = gamePlay;
    }

    public ClearResult clear() {
        userAccessor.clear();
        authAccessor.clear();
        gameAccessor.clear();
        playService.clear();
        return new ClearResult();
    }
}
