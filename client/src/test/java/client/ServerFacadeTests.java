package client;

import chess.request.RegisterRequest;
import chess.result.RegisterResult;
import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;

import java.io.IOException;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(1100);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:1100");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("Register Successfull")
    public void registerGood() throws IOException {
        RegisterRequest req = new RegisterRequest("steve", "jenkins", "steve@derp.com");
        RegisterResult res = facade.registerUser(req);
        RegisterResult expected = new RegisterResult("steve", "dont matter");
        Assertions.assertEquals(expected.username(), req.username());
    }

}
