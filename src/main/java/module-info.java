module com.example.tictactoeclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.tictactoeclient to javafx.fxml;
    exports com.example.tictactoeclient;
}