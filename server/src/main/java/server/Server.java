package server;

import spark.*;
import com.google.gson.Gson;

import dataaccess.*;
import service.*;
import service.request.*;
import service.result.*;

public class Server {

    private UserDataAccessor userAccessor;
    private AuthDataAccessor authAccessor;
    private GameDataAccessor gameAccessor;

    private ClearService clearService;
    private UserService userService;
    private Gson gson;

    public Server() {
        userAccessor = new MemoryUserDataAccessor();
        authAccessor = new MemoryAuthDataAccessor();
        gameAccessor = new MemoryGameDataAccessor();

        userService = new UserService(userAccessor, authAccessor);

        clearService = new ClearService(userAccessor, authAccessor, gameAccessor);

        gson = new Gson();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        
        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerHandler);
        Spark.post("/session", this::loginHandler);
        Spark.delete("/session", this::logoutHandler);

        Spark.delete("/db", this::clearHandler);

        Spark.post("/game", this::createGameHandler)

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }


    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object createGameHandler(Request req, Response res) {
        return null;
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
