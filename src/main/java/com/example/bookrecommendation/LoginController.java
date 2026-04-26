package com.example.bookrecommendation;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.sql.*;


public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private  Label messageLabel;

    @FXML
    protected void LoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Enter Username and Password");
            return;
        }

        // Hash the input password to check against DB
        String hashedInputPassword = PasswordUtils.hashPassword(password);

        // MODIFIED QUERY: Select 'id' as well
        String sql = "SELECT id, password FROM users WHERE username = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                int userId = rs.getInt("id"); // Get the User ID

                if (hashedInputPassword.equals(storedHashedPassword)) {
                    // SUCCESS: Create the User Session
                    UserSession.getInstance(userId, username);

                    messageLabel.setText("Login Successful!");
                    openMainWindow();
                } else {
                    messageLabel.setText("Invalid username or password.");
                }
            } else {
                messageLabel.setText("Invalid username or password.");
            }
        } catch (SQLException e) {
            messageLabel.setText("Database error. Check connection details.");
            e.printStackTrace();
        }
    }

    @FXML
    protected void SignUpLinkAction(){
        try{
            FXMLLoader fxmlLoader= new
                    FXMLLoader(getClass().getResource("signup-view.fxml"));
            Scene scene= new Scene(fxmlLoader.load());
            Stage stage= (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setTitle("SignUp Page");
        }catch ( IOException e){
            e.printStackTrace();
        }
    }
    private void openMainWindow() {
        try {
            // Close the current login window
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.close();

            // Load the home page FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Homepage-view.fxml"));
            Stage homeStage = new Stage();
            homeStage.setTitle("Homepage");
            homeStage.setScene(new Scene(fxmlLoader.load())); // Set a new scene for the home page
            homeStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

