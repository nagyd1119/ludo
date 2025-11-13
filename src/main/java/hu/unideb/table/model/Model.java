package hu.unideb.table.model;

import hu.unideb.table.model.persistence.Round;
import hu.unideb.table.model.persistence.RoundDataManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.awt.Point;
import java.io.IOException;
import java.util.*;

public class Model {
    public static final int BOARD_SIZE = 15;

    public static final Color PLAYER_1_COLOR = Color.MAROON;
    public static final Color PLAYER_2_COLOR = Color.BLUE;
    public static final Color PLAYER_3_COLOR = Color.ORANGE;
    public static final Color PLAYER_4_COLOR = Color.GREEN;

    public int numberOfPlayers;
    public Queue<Player> players = new LinkedList<>();
    public final Map<Player, List<Point>> playerPaths = new HashMap<>();
    public final Map<Player, Token[]> playerTokens = new HashMap<>();
    public Set<Point> safeZones;
    public Player currentPlayer = Player.PLAYER_1;
    public int numberOfConsecutiveSixes = 0;
    public final BooleanProperty boardChanged = new SimpleBooleanProperty(false);
    private final ObjectProperty<GameStatus> gameStatus = new SimpleObjectProperty<>(GameStatus.IN_PROGRESS);
    private List<String> playerNames;

    public GameStatus getGameStatus() {
        return gameStatus.get();
    }

    public void setGameStatus(GameStatus status) {
        gameStatus.set(status);
    }

    public ObjectProperty<GameStatus> gameStatusProperty() {
        return gameStatus;
    }

    public Model(int numberOfPlayers, List<String> playerNames) {
        this.playerNames = playerNames;
        this.numberOfPlayers = numberOfPlayers;
        if (numberOfPlayers < 2 || numberOfPlayers > 4) {
            throw new IllegalArgumentException("Invalid number of players: " + numberOfPlayers);
        }
        players.addAll(Arrays.asList(Player.values()).subList(0, numberOfPlayers));

        playerPaths.put(Player.PLAYER_1, generateBasePath());
        playerPaths.put(Player.PLAYER_2, rotatePath(generateBasePath(), 90));
        playerPaths.put(Player.PLAYER_3, rotatePath(generateBasePath(), 180));
        playerPaths.put(Player.PLAYER_4, rotatePath(generateBasePath(), 270));

        playerTokens.put(Player.PLAYER_1, new Token[]{
                new Token(new Point(11, 2), new Point(11, 2), null, Player.PLAYER_1),
                new Token(new Point(11, 3), new Point(11, 3), null, Player.PLAYER_1),
                new Token(new Point(12, 2), new Point(12, 2), null, Player.PLAYER_1),
                new Token(new Point(12, 3), new Point(12, 3), null, Player.PLAYER_1)
        });

        playerTokens.put(Player.PLAYER_2, new Token[]{
                new Token(new Point(11, 11), new Point(11, 11), null, Player.PLAYER_2),
                new Token(new Point(11, 12), new Point(11, 12), null, Player.PLAYER_2),
                new Token(new Point(12, 11), new Point(12, 11), null, Player.PLAYER_2),
                new Token(new Point(12, 12), new Point(12, 12), null, Player.PLAYER_2)
        });

//        playerTokens.put(Player.PLAYER_2, new Token[]{
//                new Token(new Point(11, 11), playerPaths.get(Player.PLAYER_2).get(playerPaths.get(Player.PLAYER_2).size()-3), null, Player.PLAYER_2),
//                new Token(new Point(11, 12), playerPaths.get(Player.PLAYER_2).get(playerPaths.get(Player.PLAYER_2).size()-10), null, Player.PLAYER_2),
//                new Token(new Point(12, 11), playerPaths.get(Player.PLAYER_2).get(playerPaths.get(Player.PLAYER_2).size()-1), null, Player.PLAYER_2),
//                new Token(new Point(12, 12), playerPaths.get(Player.PLAYER_2).get(playerPaths.get(Player.PLAYER_2).size()-5), null, Player.PLAYER_2)
//        });

        if (numberOfPlayers > 2) {
            playerTokens.put(Player.PLAYER_3, new Token[]{
                    new Token(new Point(2, 11), new Point(2, 11), null, Player.PLAYER_3),
                    new Token(new Point(2, 12), new Point(2, 12), null, Player.PLAYER_3),
                    new Token(new Point(3, 11), new Point(3, 11), null, Player.PLAYER_3),
                    new Token(new Point(3, 12), new Point(3, 12), null, Player.PLAYER_3)
            });
        }

        if (numberOfPlayers > 3) {
            playerTokens.put(Player.PLAYER_4, new Token[]{
                    new Token(new Point(2, 2), new Point(2, 2), null, Player.PLAYER_4),
                    new Token(new Point(2, 3), new Point(2, 3), null, Player.PLAYER_4),
                    new Token(new Point(3, 2), new Point(3, 2), null, Player.PLAYER_4),
                    new Token(new Point(3, 3), new Point(3, 3), null, Player.PLAYER_4)
            });
        }

        players.poll();
        players.add(Player.PLAYER_1);

        safeZones = Set.of(
                playerPaths.get(Player.PLAYER_1).getFirst(),
                playerPaths.get(Player.PLAYER_2).getFirst(),
                playerPaths.get(Player.PLAYER_3).getFirst(),
                playerPaths.get(Player.PLAYER_4).getFirst()
        );
    }

    private void markBoardChanged() {
        this.boardChanged.set(true);
    }

    public void resetBoardChanged() {
        this.boardChanged.set(false);
    }

    public int dieRoll() {
        setGameStatus(GameStatus.IN_PROGRESS);
        int die = (int) (Math.random() * 6 + 1);
        if (!tokenIsOnBoardOrMoveable()) {
            die = 6;
            Logger.info("No tokens on board or moveable, awarding 6 roll to " + currentPlayer);
        }
        checkLegalMoves(die);
        return die;

    }

    public boolean tokenIsOnBoardOrMoveable() {
        return Arrays.stream(playerTokens.get(currentPlayer))
                .anyMatch(token -> token.current != null && playerPaths.get(currentPlayer).contains(token.current) && !playerPaths.get(currentPlayer).getLast().equals(token.current));
    }

    public void checkLegalMoves(int roll) {
        Logger.info("Checking legal moves for roll: " + roll);
        clearPotentialMoves();

        if (roll == 6) {
            numberOfConsecutiveSixes++;
            if (numberOfConsecutiveSixes == 3) {
                Logger.info("Three 6s in a row! Turn skipped.");
                numberOfConsecutiveSixes = 0;
                nextPlayer();
                markBoardChanged();
                return;
            }

            boolean playerCanMove = Arrays.stream(playerTokens.get(currentPlayer)).anyMatch(token -> token.current != null && playerPaths.get(currentPlayer).contains(token.current)
                    && playerPaths.get(currentPlayer).indexOf(token.current) + roll < playerPaths.get(currentPlayer).size() || !playerPaths.get(currentPlayer).contains(token.current));

            if (!playerCanMove) {
                Logger.info("{} cannot move. Skipping turn.", currentPlayer);
                nextPlayer();
                markBoardChanged();
                setGameStatus(GameStatus.IN_PROGRESS);
                return;
            }
        } else {
            numberOfConsecutiveSixes = 0;
        }

        if (roll != 6) {
            boolean playerCanMove = Arrays.stream(playerTokens.get(currentPlayer)).anyMatch(token -> token.current != null && playerPaths.get(currentPlayer).contains(token.current)
                    && playerPaths.get(currentPlayer).indexOf(token.current) + roll < playerPaths.get(currentPlayer).size());

            if (!playerCanMove) {
                Logger.info("{} cannot move. Skipping turn.", currentPlayer);
                nextPlayer();
                markBoardChanged();
                setGameStatus(GameStatus.IN_PROGRESS);
                return;
            }
        }

        List<Point> path = playerPaths.get(currentPlayer);

        for (Token token : playerTokens.get(currentPlayer)) {

            if (token.current == null) continue;
            if (!path.contains(token.current)) {
                if (roll == 6) {
                    Point entryPoint = path.getFirst();
                    if (Arrays.stream(playerTokens.get(currentPlayer)).noneMatch(_ -> token.current.equals(entryPoint))) {
                        token.potential = path.getFirst();
                    }
                }
            } else {
                if (path.indexOf(token.current) + roll < path.size()) {
                    token.potential = path.get(path.indexOf(token.current) + roll);

                }
            }
        }
        markBoardChanged();
        setGameStatus(GameStatus.MOVING);
    }

    public void isGameOver() {
        List<Point> path = playerPaths.get(currentPlayer);
        Point goal = path.getLast();

        for (Token token : playerTokens.get(currentPlayer)) {
            if (!goal.equals(token.current)) {
                return;
            }
        }

        setGameStatus(GameStatus.valueOf(currentPlayer + "_WON"));
        saveRound();
    }

    private void clearPotentialMoves() {
        for (Map.Entry<Player, Token[]> entry : playerTokens.entrySet()) {
            entry.getValue()[0].potential = null;
            entry.getValue()[1].potential = null;
            entry.getValue()[2].potential = null;
            entry.getValue()[3].potential = null;
        }
    }

    public Color getPlayerColor(Player p) {
        return switch (p) {
            case PLAYER_1 -> PLAYER_1_COLOR;
            case PLAYER_2 -> PLAYER_2_COLOR;
            case PLAYER_3 -> PLAYER_3_COLOR;
            case PLAYER_4 -> PLAYER_4_COLOR;
        };
    }

    public void move(int row, int col) {
        Point newPosition = null;
        Point oldPosition = null;
        for (Token token : playerTokens.get(currentPlayer)) {
            if (Objects.equals(token.current, new Point(row, col)) && token.potential != null) {
                oldPosition = token.current;
                newPosition = token.potential;
                token.current = new Point(token.potential);
                token.potential = null;
                Logger.info("Move token: " + token.current);
                break;
            }
        }

        clearPotentialMoves();

        if (newPosition == null) {
            return;
        }

        List<Token> enemiesAtOldPosition = new ArrayList<>();
        List<Token> enemiesAtNewPosition = new ArrayList<>();
        for (Player p : players) {
            if (p != currentPlayer) {
                for (Token enemy : playerTokens.get(p)) {
                    if (enemy.current != null && enemy.current.equals(newPosition) && !safeZones.contains(newPosition)) {
                        enemiesAtNewPosition.add(enemy);
                    }
                    if (enemy.current != null && enemy.current.equals(oldPosition) && !safeZones.contains(oldPosition)) {
                        enemiesAtOldPosition.add(enemy);
                    }
                }
            }

            enemyTokenCapture(newPosition, enemiesAtNewPosition);
            enemyTokenCapture(oldPosition, enemiesAtOldPosition);
        }

        isGameOver();
        markBoardChanged();

        if (getGameStatus() == GameStatus.MOVING) {
            setGameStatus(GameStatus.IN_PROGRESS);
        }

        if (numberOfConsecutiveSixes == 0) {
            nextPlayer();
        }
    }

    private void enemyTokenCapture(Point position, List<Token> enemyList) {
        if (enemyList.size() == 1) {
            Token enemyToCapture = enemyList.get(0);
            Logger.info("{} captured {}’s token at ({}, {})! Sending it home.", currentPlayer, enemyToCapture.owner, position.x, position.y);
            enemyToCapture.current = new Point(enemyToCapture.origin);
            Logger.info("Extra move given to " + currentPlayer);
            setGameStatus(GameStatus.MOVING);
        } else if (enemyList.size() > 1) {
            Logger.info("Enemies protected each other at ({}, {}), no capture.", position.x, position.y);
        }
    }

    public boolean isAllowedMove(int row, int col) {
        for (Token token : playerTokens.get(currentPlayer)) {
            if (token.current != null &&
                    token.current.x == row &&
                    token.current.y == col &&
                    token.potential != null) {
                return true;
            }
        }
        return false;
    }

    public void nextPlayer() {
        currentPlayer = players.poll();
        players.add(currentPlayer);
        numberOfConsecutiveSixes = 0;
    }

    private List<Point> generateBasePath() {
        return List.of(
                new Point(8, 1), new Point(8, 2), new Point(8, 3),
                new Point(8, 4), new Point(8, 5), new Point(9, 6),
                new Point(10, 6), new Point(11, 6), new Point(12, 6),
                new Point(13, 6), new Point(14, 6), new Point(14, 7),
                new Point(14, 8), new Point(13, 8), new Point(12, 8),
                new Point(11, 8), new Point(10, 8), new Point(9, 8),
                new Point(8, 9), new Point(8, 10), new Point(8, 11),
                new Point(8, 12), new Point(8, 13), new Point(8, 14),
                new Point(7, 14), new Point(6, 14), new Point(6, 13),
                new Point(6, 12), new Point(6, 11), new Point(6, 10),
                new Point(6, 9), new Point(5, 8), new Point(4, 8),
                new Point(3, 8), new Point(2, 8), new Point(1, 8),
                new Point(0, 8), new Point(0, 7), new Point(0, 6),
                new Point(1, 6), new Point(2, 6), new Point(3, 6),
                new Point(4, 6), new Point(5, 6), new Point(6, 5),
                new Point(6, 4), new Point(6, 3), new Point(6, 2),
                new Point(6, 1), new Point(6, 0), new Point(7, 0),
                new Point(7, 1), new Point(7, 2), new Point(7, 3),
                new Point(7, 4), new Point(7, 5), new Point(7, 6)
        );
    }

    private List<Point> rotatePath(List<Point> basePath, int degrees) {
        List<Point> rotated = new ArrayList<>();
        for (Point p : basePath) {
            int x = p.x;
            int y = p.y;
            Point newPoint;

            switch (degrees) {
                case 90 -> newPoint = new Point(14 - y, x);
                case 180 -> newPoint = new Point(14 - x, 14 - y);
                case 270 -> newPoint = new Point(y, 14 - x);
                default -> newPoint = new Point(x, y);
            }
            rotated.add(newPoint);
        }
        return rotated;
    }

    private void saveRound() {
        try {
            RoundDataManager roundDataManager = new RoundDataManager();
            Round round = new Round(playerNames, playerTokens, gameStatus.get());
            roundDataManager.saveRound(round);
        } catch (IOException e) {
            Logger.error("Failed to save round data", e);
            throw new RuntimeException("Error saving round", e);
        }
    }
}
