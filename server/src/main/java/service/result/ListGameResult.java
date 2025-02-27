package service.result;

import chess.data.GameData;

import java.util.Collection;

public record ListGameResult(Collection<GameData> games) {
}
