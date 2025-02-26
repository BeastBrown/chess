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
    private UserService userService;
    private Gson gson;

    public Server() {
        userAccessor = new MemoryUserDataAccessor();
        authAccessor = new MemoryAuthDataAccessor();

        userService = new UserService(userAccessor, authAccessor);

        gson = new Gson();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        
        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerHandler);
        Spark.post("/session", this::loginHandler);
//        Spark.delete("/session", this::logoutHandler);
        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }


    public void stop() {
        Spark.stop();
        Spark.awaitStop();
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

    private Object loginHandler(Request req, Response res) {
        LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
        try {
            LoginResult loginResult = userService.loginService(loginRequest);
            res.body(gson.toJson(loginResult));
        } catch (InvalidParametersException e) {
            res.body(gson.toJson(new ErrorResult("Error: unauthorized")));
        }
        return res.body();
    }
}
