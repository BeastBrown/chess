package service;

import chess.data.GameData;
import dataaccess.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import chess.request.CreateGameRequest;
import chess.request.JoinGameRequest;
import chess.request.RegisterRequest;
import chess.result.ClearResult;
import chess.result.CreateGameResult;

import java.util.ArrayList;
import java.util.Collection;

public class ClearServiceTest {

    private UserService userService;
    private GameService gameService;
    private GamePlayService playService;
    private ClearService clearService;
    private UserDataAccessor userAccessor;
    private AuthDataAccessor authAccessor;
    private GameDataAccessor gameAccessor;
    private String testAuth;

    @BeforeEach
    public void initializeTests() throws DataAccessException {
        this.userAccessor = new MySqlUserDataAccessor();
        this.authAccessor = new MySqlAuthDataAccessor();
        this.gameAccessor = new MySqlGameDataAccessor();
        this.userService = new UserService(userAccessor, authAccessor);
        this.playService = new GamePlayService(userService, userAccessor, gameAccessor, authAccessor);
        this.clearService = new ClearService(userAccessor, authAccessor, gameAccessor, playService);
        this.gameService = new GameService(userService, gameAccessor, authAccessor);

        userAccessor.clear();
        authAccessor.clear();
        gameAccessor.clear();

        RegisterRequest registerRequest = new RegisterRequest("Bob", "shizbuckets", "derp@loler.com");
        this.testAuth = userService.registerService(registerRequest).authToken();
    }

    @Test
    @DisplayName("Clear Successful White Box")
    public void clear200() throws InvalidParametersException, InsufficientParametersException {
        CreateGameRequest createRequest = new CreateGameRequest(testAuth, "Best Game");
        CreateGameResult createResult = gameService.createGameService(createRequest);

        CreateGameRequest createRequest2 = new CreateGameRequest(testAuth, "Worst Game");
        CreateGameResult createResult2 = gameService.createGameService(createRequest2);

        JoinGameRequest joinRequest = new JoinGameRequest(testAuth, "WHITE", 1);
        gameService.joinGameService(joinRequest);

        ClearResult result =  clearService.clear();
        ClearResult expectedResult = new ClearResult();

        Collection<GameData> observedGamesDB = new ArrayList<GameData>(gameAccessor.listGames());
        Collection<GameData> expectedGamesDB = new ArrayList<GameData>();

        Assertions.assertEquals(result,expectedResult);
        Assertions.assertEquals(observedGamesDB, expectedGamesDB);
        Assertions.assertNull(authAccessor.getAuth(testAuth));
        Assertions.assertNull(userAccessor.getUser("Bob"));
    }
}
