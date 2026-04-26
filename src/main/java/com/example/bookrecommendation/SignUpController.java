package com.example.bookrecommendation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.sql.*;
import java.util.regex.Pattern;

public class SignUpController {
    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private static final Pattern email_pattern = Pattern.compile("^[A-Z0-9.%+-_]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private boolean validInput(){
        messageLabel.setText("");
        usernameField.getStyleClass().remove("error-field");
        emailField.getStyleClass().remove("error-field");
        passwordField.getStyleClass().remove("error-field");
        messageLabel.setStyle("-fx-text-fill: red;");
        //presence check
        if (usernameField.getText().isBlank() || emailField.getText().isBlank() || passwordField.getText().isBlank()) {

            messageLabel.setText("All field is required.");
            //Highlight all empty fields
            if(usernameField.getText().isBlank())
                usernameField.getStyleClass().remove("error-field");
            if(emailField.getText().isBlank())
                emailField.getStyleClass().remove("error-field");
            if(passwordField.getText().isBlank())
                passwordField.getStyleClass().remove("error-field");

            return false;
        }

        if(!email_pattern.matcher(emailField.getText()).matches()){
            messageLabel.setText("Please Enter a valid email address.");
            emailField.getStyleClass().add("error-field");
            return false;
        }

        if(passwordField.getText().length()<8){
            messageLabel.setText("password must be at least * character long.");
            passwordField.getStyleClass().add("error-field");
            return false;
        }
        return true;
    }

    @FXML
    protected void SignUpButtonAction() {
        if(!validInput()){
            return;
        }

        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String hashedPassword= PasswordUtils.hashPassword(password);


        String sql = "INSERT INTO users(username,email,password) VALUES(?,?,?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, username);
            pst.setString(2, email);
            pst.setString(3, hashedPassword);
            pst.executeUpdate();

            messageLabel.setStyle("-fx-text-fill: green;");

            messageLabel.setText("Account Created Successfully! Please Login.");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                messageLabel.setText("Username or email already exists.");
            } else {
                messageLabel.setText("Database error.");
                e.printStackTrace();
            }
        }


    }

    @FXML
    protected void LoginLinkAction(){
        try{
            FXMLLoader fxmlLoader=new
                    FXMLLoader(getClass().getResource("login-view.fxml"));

            Scene scene= new Scene(fxmlLoader.load());
            Stage stage= (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("User Login Page");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
