package com.example.tictactoeclient;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.IOException;

public class HelloApplication extends Application {

    private static final String NEW_PUBLIC_GAME = "##session##public##"; // найти случайного соперника
    private static final String NEW_PRIVATE_GAME = "##session##private##"; // создать приватную сессию
    private static final String JOIN_PRIVATE_GAME = "##session##join##"; // подключиться к приватной сессии

    private TextField username, id;
    private String connectionType;

    @Override
    public void start(Stage stage) throws IOException {

        username = new TextField();
        id = new TextField();
        Button buttonFindEnemy = new Button("Найти противника");
        Button buttonCreatePrivateGame = new Button("Создать приватную игру");
        Button buttonJoinPrivateGame = new Button("Присоединиться к приватной игре");


        buttonFindEnemy.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                connectionType = NEW_PUBLIC_GAME;
                stage.hide();
                openSecondStage();


            }
        });



        buttonCreatePrivateGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                connectionType = NEW_PRIVATE_GAME;
                stage.hide();
                openSecondStage();

            }
        });



        buttonJoinPrivateGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                connectionType = JOIN_PRIVATE_GAME;
                stage.hide();
                openSecondStage();

            }
        });


        Scene scene = new Scene(new VBox(new Text("Имя пользователя: "),username,
                new Text("Номер сеанса (если необходимо): "), id, new HBox(buttonFindEnemy, buttonCreatePrivateGame, buttonJoinPrivateGame)));

        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }

    public Stage openSecondStage() {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("game-view.fxml"));

        Scene newscene = null;
        try {
            newscene = new Scene(fxmlLoader.load(), 780, 474);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage secondStage = new Stage();
        secondStage.setTitle("TicTacToe");
        secondStage.setScene(newscene);

        GameViewController view = fxmlLoader.getController();
        view.init(username.getText(), id.getText(), connectionType);
        secondStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                view.close();
            }
        });
        secondStage.show();

        return secondStage;
    }

}