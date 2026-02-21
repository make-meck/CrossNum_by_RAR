module com.example.crossnum {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.crossnum to javafx.fxml;
    exports com.example.crossnum;
}