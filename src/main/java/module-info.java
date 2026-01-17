module com.example.crossnum {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.crossnum to javafx.fxml;
    exports com.example.crossnum;
}