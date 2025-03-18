package service;

import chess.data.GameData;
import dataaccess.*;
import org.junit.jupiter.api.*;
import chess.request.CreateGameRequest;
import chess.request.JoinGameRequest;
import chess.request.ListGameRequest;
import chess.request.RegisterRequest;
import chess.result.CreateGameResult;
import chess.result.JoinGameResult;
import chess.result.ListGameResult;

import java.util.ArrayList;

public class GameServiceTest {

    private GameService gameService;
    private UserService userService;
    private UserDataAccessor userAccessor;
    private AuthDataAccessor authAccessor;
    private GameDataAccessor gameAccessor;
    private String testAuth;

    @BeforeEach
    public void initializeTests() throws DataAccessException {
        this.userAccessor = new MySqlUserDataAccessor();
        this.authAccessor = new MySqlAuthDataAccessor();
        this.gameAccessor = new MySqlGameDataAccessor();

        userAccessor.clear();
        authAccessor.clear();
        gameAccessor.clear();

        userService = new UserService(userAccessor, authAccessor);
        gameService = new GameService(userService, gameAccessor, authAccessor);
        RegisterRequest registerRequest = new RegisterRequest("Bob", "shizbuckets", "derp@loler.com");
        this.testAuth = userService.registerService(registerRequest).authToken();
    }

    @Test()
    @DisplayName("Create Game Successful")
    public void createGame200() throws InvalidParametersException, InsufficientParametersException {
        CreateGameRequest createRequest = new CreateGameRequest(testAuth, "Best Game");
        CreateGameResult observed = gameService.createGameService(createRequest);
        CreateGameResult expected = new CreateGameResult(1);
        Assertions.assertEquals(observed, expected);

        CreateGameRequest createRequest2 = new CreateGameRequest(testAuth, "Other Game");
        CreateGameResult observed2 = gameService.createGameService(createRequest);
        CreateGameResult expected2 = new CreateGameResult(2);
        Assertions.assertEquals(observed2, expected2);
    }

    @Test
    @DisplayName("Create Game Bad Request")
    public void createGame400() {
        CreateGameRequest createRequest = new CreateGameRequest(testAuth, "");
        Assertions.assertThrows(InsufficientParametersException.class, () -> gameService.createGameService(createRequest));
    }

    @Test
    @DisplayName("Create Game Unauthorized")
    public void createGame401() {
        CreateGameRequest createRequest =
                new CreateGameRequest("no way this is a good auth", "Best Game");
        Assertions.assertThrows(InvalidParametersException.class, () -> gameService.createGameService(createRequest));
    }

    @Test
    @DisplayName("Join Game Successfull")
    public void joinGame200() throws InvalidParametersException, InsufficientParametersException {
        CreateGameRequest createRequest = new CreateGameRequest(testAuth, "Best Game");
        CreateGameResult createResult = gameService.createGameService(createRequest);

        JoinGameRequest joinRequest = new JoinGameRequest(testAuth, "WHITE", 1);
        JoinGameResult observed = gameService.joinGameService(joinRequest);
        JoinGameResult expected = new JoinGameResult();
        Assertions.assertEquals(observed, expected);
    }

    @Test
    @DisplayName("Join Game Bad Request")
    public void joinGame400() throws InvalidParametersException, InsufficientParametersException {
        CreateGameRequest createRequest = new CreateGameRequest(testAuth, "Best Game");
        CreateGameResult createResult = gameService.createGameService(createRequest);

        JoinGameRequest joinRequest = new JoinGameRequest(testAuth, "", 1);
        Assertions.assertThrows(InsufficientParametersException.class, () -> gameService.joinGameService(joinRequest));
    }

    @Test
    @DisplayName("Join Game Unauthorized")
    public void joinGame401() throws InvalidParametersException, InsufficientParametersException {
        CreateGameRequest createRequest = new CreateGameRequest(testAuth, "Best Game");
        CreateGameResult createResult = gameService.createGameService(createRequest);

        JoinGameRequest joinRequest = new JoinGameRequest("Invalid auth here", "WHITE", 1);
        Assertions.assertThrowsExactly(InvalidParametersException.class, () -> gameService.joinGameService(joinRequest), "Error: unauthorized");
    }

    @Test
    @DisplayName("Join Game Already Taken")
    public void joinGame403() throws InvalidParametersException, InsufficientParametersException {
        CreateGameRequest createRequest = new CreateGameRequest(testAuth, "Best Game");
        CreateGameResult createResult = gameService.createGameService(createRequest);

        JoinGameRequest joinRequest = new JoinGameRequest(testAuth, "WHITE", 1);
        gameService.joinGameService(joinRequest);

        JoinGameRequest joinRequest2 = new JoinGameRequest(testAuth, "WHITE", 1);
        Assertions.assertThrowsExactly(InvalidParametersException.class, () -> gameService.joinGameService(joinRequest2), "Error: already taken");
    }

    @Test
    @DisplayName("List Games Successful")
    public void listGame200() throws InvalidParametersException, InsufficientParametersException {
        CreateGameRequest createRequest = new CreateGameRequest(testAuth, "Best Game");
        CreateGameResult createResult = gameService.createGameService(createRequest);

        CreateGameRequest createRequest2 = new CreateGameRequest(testAuth, "Worst Game");
        CreateGameResult createResult2 = gameService.createGameService(createRequest2);

        JoinGameRequest joinRequest = new JoinGameRequest(testAuth, "WHITE", 1);
        gameService.joinGameService(joinRequest);

        ListGameRequest listRequest = new ListGameRequest(testAuth);

        ListGameResult observed = gameService.listGameService(listRequest);
        ArrayList<GameData> expectedList = new ArrayList<GameData>();
        expectedList.add(new GameData(1, "Bob", null, "Best Game", null));
        expectedList.add(new GameData(2, null, null, "Worst Game", null));
        ListGameResult expected = new ListGameResult(expectedList);
        Assertions.assertEquals(observed, expected);
    }

    @Test
    @DisplayName("List Games Unauthorized")
    public void listGame401() throws InvalidParametersException, InsufficientParametersException {
        CreateGameRequest createRequest = new CreateGameRequest(testAuth, "Best Game");
        CreateGameResult createResult = gameService.createGameService(createRequest);

        CreateGameRequest createRequest2 = new CreateGameRequest(testAuth, "Worst Game");
        CreateGameResult createResult2 = gameService.createGameService(createRequest2);

        JoinGameRequest joinRequest = new JoinGameRequest(testAuth, "WHITE", 1);
        gameService.joinGameService(joinRequest);

        ListGameRequest listRequest = new ListGameRequest("This is not a good auth");

        Assertions.assertThrows(InvalidParametersException.class, () -> gameService.listGameService(listRequest));
    }


}
