package hu.unideb.table.controller;

import hu.unideb.table.model.GameStatus;
import hu.unideb.table.model.Model;
import hu.unideb.table.model.Player;
import hu.unideb.table.model.Token;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TableController {

    @FXML
    private GridPane gridPane;

    @FXML
    private Button rollButton;

    @FXML
    private Label statusLabel;

    private Model model;

    private List<String> playerNames = new ArrayList<>();

    @FXML
    public void initialize() {
    }

    public void setupPlayers(int playerCount, List<String> playerNames) {
        Logger.info("Starting game with " + playerCount + " players: " + playerNames);
        this.playerNames = playerNames;

        model = new Model(playerCount, playerNames);

        model.boardChanged.addListener((_, _, newValue) -> {
            if (newValue) {
                refreshBoardDisplay();
                model.resetBoardChanged();
            }
        });

        model.gameStatusProperty().addListener((_, _, newValue) -> {
            refreshBoardDisplay();
            if (newValue == GameStatus.IN_PROGRESS) {
                rollButton.setDisable(false);
                rollButton.setText("Roll");
            }
        });

        setupGrid();
        refreshBoardDisplay();
    }

    private void setupGrid() {
        Image bgImage = new Image(Objects.requireNonNull(getClass().getResource("/board.jpg")).toExternalForm());
        BackgroundSize bgSize = new BackgroundSize(900, 900, false, false, false, false);
        BackgroundImage bg = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                bgSize
        );
        gridPane.setBackground(new Background(bg));

        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(60, 60);
                cell.setStyle("-fx-background-color: transparent;");
                int finalRow = row;
                int finalCol = col;
                cell.setOnMouseClicked(_ -> handleCellClick(finalRow, finalCol));

                gridPane.add(cell, row, col);
            }
        }
    }

    private void handleCellClick(int row, int col) {
        Logger.debug("Cell clicked at ({}, {})", row, col);
        if (model.getGameStatus() != GameStatus.MOVING && model.getGameStatus() != GameStatus.IN_PROGRESS) {
            Logger.debug("Click ignored, game over. Game status: " + model.getGameStatus());
        } else if (model.isAllowedMove(row, col)) {
            model.move(row, col);
            refreshBoardDisplay();
            rollButton.setText("Roll");
        }
    }

    private void addTokenToCell(int row, int col, Pane cell) {
        List<Token> tokensInCell = new ArrayList<>();

        for (Player player : model.playerTokens.keySet()) {
            for (Token token : model.playerTokens.get(player)) {
                if (token.current != null && token.current.x == row && token.current.y == col) {
                    tokensInCell.add(token);
                }
            }
        }

        int tokenCount = tokensInCell.size();

        if (tokenCount == 1) {
            Token token = tokensInCell.getFirst();
            cell.getChildren().add(createToken(model.getPlayerColor(token.owner)));
        } else if (tokenCount > 1) {
            double[] offsets = {
                    -10, -10,
                    10, -10,
                    -10, 10,
                    10, 10
            };
            for (int i = 0; i < tokenCount; i++) {
                Token token = tokensInCell.get(i);
                Circle tokenCircle = createToken(model.getPlayerColor(token.owner));

                double offsetX = offsets[(i * 2) % offsets.length];
                double offsetY = offsets[(i * 2 + 1) % offsets.length];

                tokenCircle.setTranslateX(offsetX);
                tokenCircle.setTranslateY(offsetY);

                cell.getChildren().add(tokenCircle);
            }
        }
    }

    private void addPotentialToCell(int row, int col, Pane cell, Player currentPlayer) {
        for (Token token : model.playerTokens.get(currentPlayer)) {
            if (token.potential != null && token.potential.x == row && token.potential.y == col) {
                cell.getChildren().add(createPotential(model.getPlayerColor(currentPlayer)));
            }
        }
    }

    private Circle createToken(Color color) {
        Circle circle = new Circle(15);
        circle.setFill(color);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        circle.parentProperty().addListener((_, _, newParent) -> {
            if (newParent instanceof Pane pane) {
                circle.centerXProperty().bind(pane.widthProperty().divide(2));
                circle.centerYProperty().bind(pane.heightProperty().divide(2));
            }
        });
        return circle;
    }

    private Circle createPotential(Color color) {
        Circle circle = new Circle(18);
        circle.setStroke(color);
        circle.setStrokeWidth(3);
        circle.setFill(Color.TRANSPARENT);
        circle.parentProperty().addListener((_, _, newParent) -> {
            if (newParent instanceof Pane pane) {
                circle.centerXProperty().bind(pane.widthProperty().divide(2));
                circle.centerYProperty().bind(pane.heightProperty().divide(2));
            }
        });
        return circle;
    }

    public void refreshBoardDisplay() {
        Logger.debug("Refreshing board display");
        for (int row = 0; row < Model.BOARD_SIZE; row++) {
            for (int col = 0; col < Model.BOARD_SIZE; col++) {
                Node node = getNodeFromGrid(row, col);
                if (node instanceof Pane cell) {
                    cell.getChildren().clear();

                    addTokenToCell(row, col, cell);
                    addPotentialToCell(row, col, cell, model.currentPlayer);
                }
            }
        }

        if (model.getGameStatus() != GameStatus.IN_PROGRESS && model.getGameStatus() != GameStatus.MOVING) {
            Logger.debug("Game over, updating label");
            Platform.runLater(() -> statusLabel.setText(getWinnerText(model.getGameStatus())));
            rollButton.setDisable(true);
        } else {
            Platform.runLater(() ->
                    statusLabel.setText(getPlayerName(model.currentPlayer) + "'s turn"));
        }
        if (model.getGameStatus() == GameStatus.IN_PROGRESS || model.getGameStatus() == GameStatus.MOVING) {
            Platform.runLater(() -> rollButton.setDisable(false));
        }
    }

    private Node getNodeFromGrid(int col, int row) {
        for (Node node : gridPane.getChildren()) {
            Integer nodeRow = GridPane.getRowIndex(node);
            Integer nodeCol = GridPane.getColumnIndex(node);

            if (nodeRow == null) nodeRow = 0;
            if (nodeCol == null) nodeCol = 0;

            if (nodeRow == row && nodeCol == col) {
                return node;
            }
        }
        return null;
    }

    private String getPlayerName(Player player) {
        int index = player.ordinal();
        if (index >= 0 && index < playerNames.size()) {
            return playerNames.get(index);
        }
        return player.toString();
    }

    private String getWinnerText(GameStatus gameStatus) {
        return switch (gameStatus) {
            case PLAYER_1_WON -> playerNames.get(0) + " won!";
            case PLAYER_2_WON -> playerNames.get(1) + " won!";
            case PLAYER_3_WON -> playerNames.get(2) + " won!";
            case PLAYER_4_WON -> playerNames.get(3) + " won!";
            default -> null;
        };
    }

    public void handleRollClick(ActionEvent actionEvent) {
        if (model.getGameStatus() == GameStatus.IN_PROGRESS) {
            int rolled = model.dieRoll();
            rollButton.setText("🎲 " + rolled);

            rollButton.setDisable(rolled != 6);
        }
    }
}
