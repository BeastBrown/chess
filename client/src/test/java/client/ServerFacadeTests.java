package client;

import chess.data.GameData;
import chess.request.*;
import chess.result.*;
import org.junit.jupiter.api.*;
import server.Server;
import ui.Client;
import ui.ServerFacade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        Client client = new Client("http://localhost:1100");
        server = new Server();
        var port = server.run(1100);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:1100", client);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearServer() throws IOException {
        facade.clearDatabase();
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("Register Successfull")
    public void registerGood() throws IOException {
        RegisterResult req = registerRandomUser();
        RegisterResult expected = new RegisterResult("steve", "dont matter");
        Assertions.assertEquals(expected.username(), req.username());
    }

    private static RegisterResult registerRandomUser() throws IOException {
        RegisterRequest req = new RegisterRequest("steve", "jenkins", "steve@derp.com");
        RegisterResult res = facade.registerUser(req);
        return res;
    }

    @Test
    @DisplayName("Register Taken")
    public void registerUserTaken() throws IOException {
        registerRandomUser();
        Assertions.assertThrows(IOException.class, ServerFacadeTests::registerRandomUser);
    }

    @Test
    @DisplayName("Logged in successful")
    public void loginGood() throws IOException {
        registerRandomUser();
        LoginRequest req = new LoginRequest("steve", "jenkins");
        LoginResult res = facade.loginUser(req);
        LoginResult expected = new LoginResult("steve", "who cares");
        Assertions.assertEquals(expected.username(), res.username());
    }

    @Test
    @DisplayName("Login User doesnt exist")
    public void loginBad() {
        LoginRequest req = new LoginRequest("steve", "jenkins");
        Assertions.assertThrows(IOException.class, () -> facade.loginUser(req));
    }

    @Test
    @DisplayName("Logout Successful")
    public void logoutGood() throws IOException {
        String auth = registerRandomUser().authToken();
        LogoutResult observed = facade.logoutUser(new LogoutRequest(auth));
        Assertions.assertEquals(new LogoutResult(), observed);
    }

    @Test
    @DisplayName("Logout while not logged in")
    public void logoutBad() throws IOException {
        String auth = registerRandomUser().authToken();
        facade.logoutUser(new LogoutRequest(auth));
        Assertions.assertThrows(IOException.class, () -> facade.logoutUser(new LogoutRequest(auth)));
    }

    @Test
    @DisplayName("Create game successful")
    public void createGamesGood() throws IOException {
        String auth = registerRandomUser().authToken();
        CreateGameResult observed = createOpGame(auth);
        CreateGameResult expected = new CreateGameResult(1);
        Assertions.assertEquals(expected, observed);
    }

    private static CreateGameResult createOpGame(String auth) throws IOException {
        CreateGameRequest gameReq = new CreateGameRequest(auth, "OP GAME");
        return facade.createGame(gameReq);
    }

    @Test
    @DisplayName("Create game unauthorized")
    public void createGamesBad() throws IOException {
        String auth = registerRandomUser().authToken();
        CreateGameRequest gameBadReq =
                new CreateGameRequest("Likely not the auth", "OP GAME");
        Assertions.assertThrows(IOException.class, () -> facade.createGame(gameBadReq));
    }

    @Test
    @DisplayName("List Games Successful")
    public void listGamesGood() throws IOException {
        String auth = registerRandomUser().authToken();
        CreateGameResult gameResult = createOpGame(auth);
        ListGameRequest req = new ListGameRequest(auth);
        ListGameResult observed = facade.listGames(req);
        ArrayList<GameData> expectedList = new ArrayList<GameData>();
        expectedList.add(new GameData(1,null,null,"OP GAME", null));
        ListGameResult expected = new ListGameResult(expectedList);
        Assertions.assertEquals(expected, observed);
    }

    @Test
    @DisplayName("Join Game Successful")
    public void joinGameGood() throws IOException {
        String auth = registerRandomUser().authToken();
        CreateGameResult gameResult = createOpGame(auth);
        JoinGameResult observed = facade.joinGame(new JoinGameRequest(auth, "WHITE", 1));
        JoinGameResult expected = new JoinGameResult();
        Assertions.assertEquals(expected, observed);
        ArrayList<GameData> games = (ArrayList<GameData>) facade.listGames(new ListGameRequest(auth)).games();
        GameData newGameObserved = games.get(0);
        GameData newGameExpected = new GameData(1, "steve",null,"OP GAME",null);
        Assertions.assertEquals(newGameExpected, newGameObserved);
    }

    @Test
    @DisplayName("Join Game Already taken")
    public void joinGameBad() throws IOException {
        String auth = registerRandomUser().authToken();
        CreateGameResult gameResult = createOpGame(auth);
        JoinGameRequest joinBadReq = new JoinGameRequest(auth, "WHITE", 1);
        facade.joinGame(joinBadReq);
        Assertions.assertThrows(IOException.class, () -> facade.joinGame(joinBadReq));
    }

}
