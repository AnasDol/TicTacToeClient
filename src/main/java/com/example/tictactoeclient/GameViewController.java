package com.example.tictactoeclient;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class GameViewController {

    private Connection connection;

    // размеры поля
    private final double fieldWidth = 472;
    private final double fieldHeight = 472;

    // поле
    private int dimension = 3;
    private Cell[][] cells;

    // чей ход
    private static boolean myTurn = false;
    public static void setMyTurn(boolean b) { myTurn = b; }
    public void setTurn(String player) {
        myTurn = player.equalsIgnoreCase(connection.getUsername());
        turnText.setText("Ход игрока: "+player);
    }

    // красивости
    private Color randColor;

    @FXML
    private GridPane gridPane; // сетка
    @FXML
    private Text usernameText, idText, turnText;
    @FXML
    private TextField messageTextField;
    @FXML
    private Text historyText;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Button buttonNewGame;
    public void disableButtonNewGame() {buttonNewGame.setDisable(true);}

    public void init(String username, String id, String connectionType) {

        this.connection = new Connection(this, username, id, connectionType);

       // Bindings.bindBidirectional(idText.textProperty(), connection.getSessionIdStr());
        Bindings.bindBidirectional(usernameText.textProperty(), connection.getUsernameStr());
        Bindings.bindBidirectional(connection.getHistory(), historyText.textProperty());

        // настройка числа и размера строк и столбцов игрового поля
        for (int i = 0; i< dimension; i++) {
            gridPane.getColumnConstraints().add(new ColumnConstraints(fieldWidth/ dimension));
            gridPane.getRowConstraints().add(new RowConstraints(fieldHeight/ dimension));
        }

        // рандомный цвет
        randColor = new Color(Math.random(), Math.random(), Math.random(), 0.9);

        // заполнение сетки кнопками
        cells = new Cell[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                cells[i][j] = new Cell(i,j);
                gridPane.add(cells[i][j], j, i, 1, 1);
            }
        }

    }

    public void sendMessage() {
        connection.sendUserMessage(messageTextField.getText());
        scrollPane.setVvalue( 1.0d );
    }

    public void markCell(int cellPos, String symbol) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                cells[cellPos/10][cellPos%10].mark(symbol);
            }
        });
    }

    // запрос на очистку поля
    public void newGame() {
        connection.sendMessage("##newgame##");
    }

    public void clearField(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < dimension; i++) {
                    for (int j = 0; j < dimension; j++) {
                        cells[i][j].release();
                    }
                }
            }
        });

    }

    public void close() {
        connection.close();
    }

    public void setSessionId(String sessionId) {
        idText.setText(sessionId);
    }


    public class Cell extends Button {

        private int i, j; // номер строки и столбца соответственно

        public Cell(int i, int j) {

            super();

            this.i = i;
            this.j = j;

            setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    seize();
                }
            });

            String rgb = String.format("%d, %d, %d",
                    (int) (randColor.getRed() * 255),
                    (int) (randColor.getGreen() * 255),
                    (int) (randColor.getBlue() * 255));
            this.setStyle("-fx-font-size: 50; -fx-background-color: rgba(" + rgb + ", 0.25)");
            this.setPrefSize(fieldWidth/ dimension,fieldHeight/ dimension);

        }

        public void seize() {
            if (!myTurn) return;
            connection.sendMessage("##mark##");
            connection.sendMessage(Integer.toString(i)+Integer.toString(j));
        }

        public void release() {
            this.setDisable(false);
            this.setText("");
        }

        public void mark(String symbol) {
            this.setDisable(true);
            this.setText(symbol);

        }


    }


}
