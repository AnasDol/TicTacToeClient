package com.example.tictactoeclient;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

public class Connection {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3443;

    private Socket clientSocket;
    private Scanner inMessage;
    private PrintWriter outMessage;

    private Thread thread;

    private String username, sessionId;
    public StringProperty getUsernameStr() {return new SimpleStringProperty("Имя пользователя: " + username);}
    public StringProperty getSessionIdStr() {return new SimpleStringProperty("Идентификатор сессии: " + sessionId);}
    public String getUsername() { return username; }

    private StringProperty history;
    public StringProperty getHistory() {return history;}

    private boolean needSessionId = false;
    private boolean nosession = false;

    private GameViewController gameViewController;

    public Connection(GameViewController gViewController, String uname, String sesId, String connectionType) {

        username = uname;
        sessionId = sesId;
        gameViewController = gViewController;

        history = new SimpleStringProperty();

        try {
            // подключаемся к серверу
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            inMessage = new Scanner(clientSocket.getInputStream());
            outMessage = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            alarm("Не удалось подсоединиться к серверу");
        }

        outMessage.println(connectionType); // сообщаем серверу о своем типе подключения
        outMessage.flush();

        if (connectionType.equalsIgnoreCase("##session##join##")) {
            outMessage.println(sesId); // отправляем id
            outMessage.flush();
        }

        // отправляем имя пользователя
        outMessage.println("##username##");
        outMessage.flush();
        outMessage.println(username);
        outMessage.flush();

        thread  = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // бесконечный цикл
                    while (!Thread.interrupted()) {
                        // если есть входящее сообщение
                        if (inMessage.hasNext()) {
                            // считываем его
                            String inMes = inMessage.nextLine();
                            System.out.println(inMes);

                            if (inMes.equalsIgnoreCase("##sessionId##")) {
                                sessionId = waitForMessage();
                                gViewController.setSessionId("Идентификатор сессии: " + sessionId);
                            }

                            else if (inMes.equalsIgnoreCase("##error##nosession##")) {
                                nosession = true;
                                close();
                            }

                            else if (inMes.equalsIgnoreCase("##turn##")) {
                                gViewController.setTurn(waitForMessage());
                            }

                            else if (inMes.equalsIgnoreCase("##cell##")) {
                                gameViewController.markCell(Integer.parseInt(waitForMessage()), waitForMessage());
                            }

                            else if (inMes.equalsIgnoreCase("##clear##")) {
                                gameViewController.clearField();
                            }

                            else if (inMes.equalsIgnoreCase("##observer##")) {
                                gViewController.disableButtonNewGame();
                                history.setValue(history.getValue() + "\n" + "Внимание, вы находитесь в роли наблюдателя. Чтобы зайти как игрок, дождитесь освободившегося места и переподключитесь к сессии");
                            }

                            else {
                                history.setValue(history.getValue() + "\n" + inMes);
                            }
                        }
                    }
                } catch (Exception e) {e.printStackTrace();}
            }
        });

        thread.start();


    }

    public void close() {

        // отправляем служебное сообщение, которое является признаком того, что клиент вышел из чата
        outMessage.println("##session##end##");
        outMessage.flush();
        thread.interrupt();
        outMessage.close();
        inMessage.close();
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (nosession) {
            alarm("Сессия с указанным идентификатором не найдена");
            nosession = false;
        }

    }

    public void sendMessage(String msg) {
        outMessage.flush();
        outMessage.println(msg);
        outMessage.flush();
        //history.setValue(history.getValue() + "\n" + username + ": " + msg);
    }

    public void sendUserMessage(String msg) {
        outMessage.println("##message##");
        outMessage.flush();
        outMessage.println(msg);
        outMessage.flush();
        //history.setValue(history.getValue() + "\n" + username + ": " + msg);
    }


    public void alarm(String msg) {

        // жалуется

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Pause");
        alert.setHeaderText("Statistics: ");
        alert.setContentText(msg);
        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == ButtonType.CANCEL) {
            System.out.println("cansel pressed");
        }


    }

    public String waitForMessage() {
        String clientMsg = "";
        while(true){
            if (inMessage.hasNext()) {
                clientMsg = inMessage.nextLine();
                break;
            }
        }
        System.out.println("receiving: " + clientMsg);
        return clientMsg;
    }

}
