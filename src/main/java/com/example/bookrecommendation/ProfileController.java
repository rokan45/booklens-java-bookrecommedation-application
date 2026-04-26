package com.example.bookrecommendation;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label totalBooksLabel;
    @FXML private Label completedBooksLabel;
    @FXML private Label avgRatingLabel;
    @FXML private Label favGenreLabel;
    @FXML private Button logoutButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadUserProfile();
    }

    private void loadUserProfile() {
        UserSession session = UserSession.getInstance();
        if (session == null) return;

        usernameLabel.setText("@" + session.getUsername());
        // In a real app, you'd fetch the email from DB here too, but for now:
        emailLabel.setText("User ID: " + session.getUserId());

        int userId = session.getUserId();

        try (Connection conn = DatabaseConnection.getConnection()) {

            // 1. Count Total Books
            String countSql = "SELECT COUNT(*) FROM user_books WHERE user_id = ?";
            PreparedStatement ps1 = conn.prepareStatement(countSql);
            ps1.setInt(1, userId);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) totalBooksLabel.setText(String.valueOf(rs1.getInt(1)));

            // 2. Count Completed Books
            String completedSql = "SELECT COUNT(*) FROM user_books WHERE user_id = ? AND status = 'COMPLETED'";
            PreparedStatement ps2 = conn.prepareStatement(completedSql);
            ps2.setInt(1, userId);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) completedBooksLabel.setText(String.valueOf(rs2.getInt(1)));

            // 3. Calculate Average Rating Given
            String avgSql = "SELECT AVG(rating) FROM user_books WHERE user_id = ? AND rating IS NOT NULL";
            PreparedStatement ps3 = conn.prepareStatement(avgSql);
            ps3.setInt(1, userId);
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) {
                double avg = rs3.getDouble(1);
                avgRatingLabel.setText(String.format("%.1f", avg));
            }

            // 4. Find Favorite Genre
            String genreSql = "SELECT b.genre, COUNT(*) as c FROM user_books ub " +
                    "JOIN books b ON ub.book_id = b.id " +
                    "WHERE ub.user_id = ? " +
                    "GROUP BY b.genre ORDER BY c DESC LIMIT 1";
            PreparedStatement ps4 = conn.prepareStatement(genreSql);
            ps4.setInt(1, userId);
            ResultSet rs4 = ps4.executeQuery();
            if (rs4.next()) {
                favGenreLabel.setText(rs4.getString("genre"));
            } else {
                favGenreLabel.setText("None yet");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bookrecommendation/homepage-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        // Clear the user session
        UserSession.getInstance().cleanUserSession();

        try {
            // Load the login view
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bookrecommendation/login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the current stage (window)
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Book Recommender - Login");
            stage.setMaximized(true); // Ensure login page is maximized

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}