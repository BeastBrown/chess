package chess.request;

public record CreateGameRequest(String authToken, String gameName) {
}
