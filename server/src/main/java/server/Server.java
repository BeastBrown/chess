package server;

import chess.request.*;
import chess.result.*;
import spark.*;
import com.google.gson.Gson;

import dataaccess.*;
import service.*;

public class Server {

    private UserDataAccessor userAccessor;
    private AuthDataAccessor authAccessor;
    private GameDataAccessor gameAccessor;
    private GameService gameService;
    private ClearService clearService;
    private UserService userService;
    private GamePlayService gamePlayService;
    private Gson gson;

    public Server() {

        try {
            userAccessor = new MySqlUserDataAccessor();
            authAccessor = new MySqlAuthDataAccessor();
            gameAccessor = new MySqlGameDataAccessor();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        userService = new UserService(userAccessor, authAccessor);
        gameService = new GameService(userService, gameAccessor, authAccessor);
        clearService = new ClearService(userAccessor, authAccessor, gameAccessor);
        gamePlayService = new GamePlayService(userService, userAccessor, gameAccessor, authAccessor);

        gson = new Gson();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        
        Spark.staticFiles.location("web");

        WebSocketServer wsHandler = new WebSocketServer(gamePlayService);
        Spark.webSocket("/ws", wsHandler);
        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerHandler);
        Spark.post("/session", this::loginHandler);
        Spark.delete("/session", this::logoutHandler);

        Spark.delete("/db", this::clearHandler);

        Spark.post("/game", this::createGameHandler);
        Spark.put("/game", this::joinGameHandler);
        Spark.get("/game", this::listGameHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }


    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object listGameHandler(Request req, Response res) {
        ListGameRequest listRequest = new ListGameRequest(req.headers("authorization"));
        try {
            ListGameResult listResult = gameService.listGameService(listRequest);
            res.body(gson.toJson(listResult));
        } catch (InvalidParametersException e) {
            res.body(gson.toJson(new ErrorResult("Error: unauthorized")));
            res.status(401);
        }
        return res.body();
    }

    private Object joinGameHandler(Request req, Response res) {
        DesiredGame desiredGame = gson.fromJson(req.body(), DesiredGame.class);
        JoinGameRequest joinRequest = new JoinGameRequest(req.headers("authorization"),
                desiredGame.playerColor(), desiredGame.gameID());
        try {
            JoinGameResult joinResult = gameService.joinGameService(joinRequest);
            res.body(gson.toJson(joinResult));
        } catch (InsufficientParametersException e) {
            res.body(gson.toJson(new ErrorResult("Error: bad request")));
            res.status(400);
        } catch (InvalidParametersException e) {
            int status = e.getMessage().equals("Error: unauthorized") ? 401 : 403;
            res.body(gson.toJson(new ErrorResult(e.getMessage())));
            res.status(status);
        }
        return res.body();
    }

    private Object createGameHandler(Request req, Response res) {
        String name = gson.fromJson(req.body(), GameName.class).gameName();
        CreateGameRequest createRequest = new CreateGameRequest(req.headers("authorization"), name);
        try {
            CreateGameResult createResult = gameService.createGameService(createRequest);
            res.body(gson.toJson(createResult));
        } catch (InvalidParametersException e) {
            res.body(gson.toJson(new ErrorResult("Error: unauthorized")));
            res.status(401);
        } catch (InsufficientParametersException e) {
            res.body(gson.toJson(new ErrorResult("Error: bad request")));
            res.status(400);
        }
        return res.body();
    }

    private Object clearHandler(Request req, Response res) {
        ClearResult clearResult = clearService.clear();
        res.body(gson.toJson(clearResult));
        return res.body();
    }

    private String registerHandler(Request req, Response res) {
        RegisterRequest registerRequest = gson.fromJson(req.body(), RegisterRequest.class);
        try {
            RegisterResult result = userService.registerService(registerRequest);
            res.body(gson.toJson(result, RegisterResult.class));
        } catch (InsufficientParametersException e) {
            ErrorResult errorResult400 = new ErrorResult("Error: bad request");
            res.status(400);
            res.body(gson.toJson(errorResult400));
        } catch (InvalidParametersException e) {
            ErrorResult errorResult403 = new ErrorResult("Error: already taken");
            res.status(403);
            res.body(gson.toJson(errorResult403));
        }

        return res.body();
    }

    private String loginHandler(Request req, Response res) {
        LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
        try {
            LoginResult loginResult = userService.loginService(loginRequest);
            res.body(gson.toJson(loginResult));
        } catch (InvalidParametersException e) {
            res.body(gson.toJson(new ErrorResult("Error: unauthorized")));
            res.status(401);
        }
        return res.body();
    }

    private String logoutHandler(Request req, Response res) {
        LogoutRequest logoutRequest = new LogoutRequest(req.headers("authorization"));
        try {
            LogoutResult logoutResult = userService.logoutService(logoutRequest);
            res.body(gson.toJson(logoutResult));
        } catch (InvalidParametersException e) {
            res.body(gson.toJson(new ErrorResult("Error: unauthorized")));
            res.status(401);
        }
        return res.body();
    }
}
