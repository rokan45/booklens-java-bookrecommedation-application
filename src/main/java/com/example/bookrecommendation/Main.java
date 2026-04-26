package com.example.bookrecommendation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("/com/example/bookrecommendation/login-view.fxml")
        );

        // In Main.java's start method
        stage.setTitle("Book Recommender - Login");
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setMaximized(true); // Launch maximized
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
