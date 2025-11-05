package hu.unideb.table.model.persistence;

import hu.unideb.table.model.GameStatus;
import hu.unideb.table.model.Player;
import hu.unideb.table.model.Token;

import java.util.List;
import java.util.Map;

public class Round{
    public List<String> playerNames;
    Map<Player, Token[]> playerTokens;
    public GameStatus status;

    public Round(List<String> playerNames, Map<Player, Token[]> playerTokens, GameStatus status) {
        this.playerNames = playerNames;
        this.playerTokens = playerTokens;
        this.status = status;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public void setPlayerNames(List<String> playerNames) {
        this.playerNames = playerNames;
    }

    public Map<Player, Token[]> getPlayerTokens() {
        return playerTokens;
    }

    public void setPlayerTokens(Map<Player, Token[]> playerTokens) {
        this.playerTokens = playerTokens;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Round{" + "playerNames=" + playerNames +
                ", playerTokens=" + playerTokens +
                ", status=" + status +
                '}';
    }
}