module com.example.bookrecommendation.bookrecommendation {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires java.desktop;


    opens com.example.bookrecommendation to javafx.fxml;
    exports com.example.bookrecommendation;
}