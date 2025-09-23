//package com.example.table;
//
//import javafx.fxml.FXML;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.Pane;
//
//public class TableController {
//
//    @FXML
//    private GridPane gridPane;
//
//    @FXML
//    public void initialize() {
//        for (int row = 0; row < 15; row++) {
//            for (int col = 0; col < 15; col++) {
//                Pane cell = new Pane();
//                cell.setPrefSize(500, 500); // minden cella mérete
//                if ((row >= 6 && row <=8) ||  (col >= 6 && col <=8)) {
//                    if (row == 8 && col == 14) {
//                        cell.setStyle("-fx-background-color: red; -fx-border-color: grey");
//                    } else if (row == 0 && col == 8) {
//                        cell.setStyle("-fx-background-color: yellow; -fx-border-color: grey");
//                    } else if (row == 6 && col == 0) {
//                        cell.setStyle("-fx-background-color: green; -fx-border-color: grey");
//                    } else if (row == 14 && col == 6) {
//                        cell.setStyle("-fx-background-color: blue; -fx-border-color: grey");
//                    } else {
//                        cell.setStyle("-fx-background-color: lightgrey; -fx-border-color: grey;");
//                    }
//
//                    if (row == 7 && (col <= 5 && col >= 1)){
//                        cell.setStyle("-fx-background-color: green; -fx-border-color: grey");
//                    } else if (row == 7 && (col <= 13 && col >= 9)){
//                        cell.setStyle("-fx-background-color: red; -fx-border-color: grey");
//                    } else if (col == 7 && (row <= 13 && row >= 9)) {
//                        cell.setStyle("-fx-background-color: blue; -fx-border-color: grey");
//                    } else if (col == 7 && (row <= 5 && row >= 1)) {
//                        cell.setStyle("-fx-background-color: yellow; -fx-border-color: grey");
//                    }
//
//                } else {
//                    cell.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
//                }
//                gridPane.add(cell, col, row);
//            }
//        }
//    }
//}

package com.example.table;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.util.Objects;

public class TableController {

    @FXML
    private GridPane gridPane;

    @FXML
    public void initialize() {
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(600, 600);

                cell.setStyle("-fx-background-color: transparent; -fx-border-color: grey;");

                gridPane.add(cell, col, row);
            }
        }
    }
}
