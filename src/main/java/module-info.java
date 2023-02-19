module com.example.argumentgames {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.argumentgames to javafx.fxml;
    exports com.example.argumentgames;
}