package hu.unideb.table.controller;

import hu.unideb.table.model.GameStatus;
import hu.unideb.table.model.persistence.Round;
import hu.unideb.table.model.persistence.RoundDataManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML
    private ChoiceBox<Integer> playerCountChoice;

    @FXML
    private VBox nameFieldsContainer;

    @FXML
    private Button startButton;

    private final List<TextField> nameFields = new ArrayList<>();
    private final RoundDataManager roundDataManager = new RoundDataManager();
    private final List<Label> statsLabels = new ArrayList<>();

    @FXML
    public void initialize() {
        playerCountChoice.getItems().addAll(2, 3, 4);
        playerCountChoice.setOnAction(_ -> createNameFields(playerCountChoice.getValue()));
        startButton.setDisable(true);
    }

    private void createNameFields(int playerCount) {
        nameFieldsContainer.getChildren().clear();
        nameFields.clear();
        statsLabels.clear();

        for (int i = 1; i <= playerCount; i++) {
            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER);

            TextField nameField = new TextField();
            nameField.setPromptText("Player " + i + " name");

            Label statsLabel = new Label("Wins: 0 / Rounds: 0");
            statsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4b2e05;");

            nameField.textProperty().addListener((_, _, newVal) -> {
                updatePlayerStats(newVal, statsLabel);
                checkIfReady();
            });

            nameFields.add(nameField);
            statsLabels.add(statsLabel);

            row.getChildren().addAll(nameField, statsLabel);
            nameFieldsContainer.getChildren().add(row);
        }

        checkIfReady();
    }

    private void checkIfReady() {
        boolean allFilled = nameFields.stream().noneMatch(f -> f.getText().trim().isEmpty());
        startButton.setDisable(!allFilled);
    }

    @FXML
    private void handleStartClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hu/unideb/table/table.fxml"));
            Parent root = loader.load();

            TableController controller = loader.getController();
            controller.setupPlayers(playerCountChoice.getValue(),
                    nameFields.stream().map(TextField::getText).toList());

            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ludo");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePlayerStats(String playerName, Label statsLabel) {
        if (playerName == null || playerName.isBlank()) {
            statsLabel.setText("Wins: 0 / Rounds: 0");
            return;
        }

        try {
            List<Round> rounds = roundDataManager.loadRounds();
            int wins = 0;
            int totalRounds = 0;

            for (Round round : rounds) {
                for (String name : round.getPlayerNames()){
                    if (playerName.equalsIgnoreCase(name)){
                        totalRounds++;
                        if ((round.getStatus()== GameStatus.PLAYER_1_WON && playerName.equals(round.getPlayerNames().get(0)))
                        || (round.getStatus()== GameStatus.PLAYER_2_WON && playerName.equals(round.getPlayerNames().get(1)))
                        || (round.getStatus()== GameStatus.PLAYER_3_WON && playerName.equals(round.getPlayerNames().get(2)))
                        || (round.getStatus()== GameStatus.PLAYER_4_WON && playerName.equals(round.getPlayerNames().get(3)))){
                            wins++;
                        }
                    }
                }
            }
            statsLabel.setText("Wins: " + wins + " / Rounds: " + totalRounds);
        } catch (Exception e) {
            statsLabel.setText("Wins: 0 / Rounds: 0");
            Logger.warn(e, "Failed to load rounds for player stats");
        }
    }
}
