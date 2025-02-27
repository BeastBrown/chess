package service;

import chess.data.GameData;
import chess.ChessGame;
import dataaccess.AuthDataAccessor;
import dataaccess.GameDataAccessor;
import dataaccess.InsufficientParametersException;
import dataaccess.InvalidParametersException;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.request.ListGameRequest;
import service.result.ClearResult;
import service.result.CreateGameResult;
import service.result.JoinGameResult;
import service.result.ListGameResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class GameService {

    private UserService userService;
    private GameDataAccessor gameAccessor;
    private AuthDataAccessor authAccessor;

    public GameService(UserService userService, GameDataAccessor gameAccessor, AuthDataAccessor authAccessor) {
        this.userService = userService;
        this.gameAccessor = gameAccessor;
        this.authAccessor = authAccessor;
    }

    public CreateGameResult createGameService(CreateGameRequest createRequest)
            throws InvalidParametersException, InsufficientParametersException {
        if (!userService.isAuthenticated(createRequest.authToken())) {
            throw new InvalidParametersException("Auth token is not valid");
        }
        if (createRequest.gameName() == null || createRequest.gameName().isEmpty()) {
            throw new InsufficientParametersException("the game name cannot be empty");
        }

        Integer newGameID = gameAccessor.getCounterID();
        GameData newGame = new GameData(newGameID, null, null,
                createRequest.gameName(), new ChessGame());
        gameAccessor.createGame(newGame);
        return new CreateGameResult(newGameID);
    }

    public JoinGameResult joinGameService(JoinGameRequest joinRequest)
            throws InsufficientParametersException, InvalidParametersException {
        validateJoinRequest(joinRequest);
        GameData gameData = gameAccessor.getGame(joinRequest.gameID());
        String username = authAccessor.getAuth(joinRequest.authToken()).username();
        GameData newGame = switch(joinRequest.playerColor()) {
            case "WHITE" -> new GameData(joinRequest.gameID(), username, gameData.blackUsername(),
                    gameData.gameName(), gameData.game());
            default -> new GameData(joinRequest.gameID(), gameData.whiteUsername(), username,
                    gameData.gameName(), gameData.game());
        };
        gameAccessor.updateGameData(newGame);
        return new JoinGameResult();
    }

    private void validateJoinRequest(JoinGameRequest joinRequest)
            throws InvalidParametersException, InsufficientParametersException {
        if (!userService.isAuthenticated(joinRequest.authToken())) {
            throw new InvalidParametersException("Error: unauthorized");
        }
        if (isBadJoinRequest(joinRequest)) {
            throw new InsufficientParametersException("Necessary field are empty or invalid");
        }
        GameData gameData = gameAccessor.getGame(joinRequest.gameID());
        String desiredColorOccupant = joinRequest.playerColor().equals("WHITE")
                ? gameData.whiteUsername() : gameData.blackUsername();
        if (desiredColorOccupant != null) {
            throw new InvalidParametersException("Error: already taken");
        }
    }

    private boolean isBadJoinRequest(JoinGameRequest joinRequest) {
        return gameAccessor.getGame(joinRequest.gameID()) == null ||
                !(Objects.equals(joinRequest.playerColor(), "WHITE") ||
                        Objects.equals(joinRequest.playerColor(), "BLACK"));
    }

    public ListGameResult listGameService(ListGameRequest listRequest)
            throws InvalidParametersException {
        if (!userService.isAuthenticated(listRequest.authToken())) {
            throw new InvalidParametersException("Not authorized");
        }
        return new ListGameResult(getGameCollection());
    }

    private ArrayList<GameData> getGameCollection() {
        ArrayList<GameData> gameList= new ArrayList<GameData>(gameAccessor.listGames());
        for (int i = 0; i < gameList.size(); i++) {
            GameData ogGame = gameList.get(i);
            GameData gameEmpty = new GameData(ogGame.gameID(), ogGame.whiteUsername(), ogGame.blackUsername(), ogGame.gameName(), null);
            gameList.set(i, gameEmpty);
        }
        return gameList;
    }
}
