package service;

import chess.data.GameData;
import dataaccess.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.request.RegisterRequest;
import service.result.ClearResult;
import service.result.CreateGameResult;

import java.util.ArrayList;
import java.util.Collection;

public class ClearServiceTest {

    private UserService userService;
    private GameService gameService;
    private ClearService clearService;
    private UserDataAccessor userAccessor;
    private AuthDataAccessor authAccessor;
    private GameDataAccessor gameAccessor;
    private String testAuth;

    @BeforeEach
    public void initializeTests() throws InvalidParametersException, InsufficientParametersException {
        this.userAccessor = new MemoryUserDataAccessor();
        this.authAccessor = new MemoryAuthDataAccessor();
        this.gameAccessor = new MemoryGameDataAccessor();
        this.clearService = new ClearService(userAccessor, authAccessor, gameAccessor);
        this.userService = new UserService(userAccessor, authAccessor);
        this.gameService = new GameService(userService, gameAccessor, authAccessor);

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
