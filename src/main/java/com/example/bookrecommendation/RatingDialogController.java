package com.example.bookrecommendation;


import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RatingDialogController {

    @FXML private HBox starContainer;

    private int userBookId;
    private int selectedRating = 0; // Default to 0, means no rating yet
    private BookshelfController bookshelfController; // Reference to refresh parent


    public void initData(int userBookId, int initialRating, BookshelfController parentController) {
        this.userBookId = userBookId;
        this.selectedRating = initialRating;
        this.bookshelfController = parentController;
        setupStarButtons();
    }

    private void setupStarButtons() {
        starContainer.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            Button star = new Button();
            final int ratingValue = i;
            updateStarStyle(star, i, selectedRating); // Set initial style

            star.setOnAction(e -> {
                selectedRating = ratingValue;
                updateStarButtonsDisplay(); // Redraw stars based on new selection
            });
            starContainer.getChildren().add(star);
        }
    }

    // Helper to update star button text/color
    private void updateStarStyle(Button starButton, int starNumber, int currentSelectedRating) {
        if (starNumber <= currentSelectedRating) {
            starButton.setText("★"); // Filled Star
            starButton.setStyle("-fx-background-color: transparent; -fx-text-fill: gold; -fx-font-size: 24px; -fx-padding: 0;");
        } else {
            starButton.setText("☆"); // Empty Star
            starButton.setStyle("-fx-background-color: transparent; -fx-text-fill: gray; -fx-font-size: 24px; -fx-padding: 0;");
        }
    }

    // Redraws all stars after a new selection
    private void updateStarButtonsDisplay() {
        for (int i = 0; i < starContainer.getChildren().size(); i++) {
            Button star = (Button) starContainer.getChildren().get(i);
            updateStarStyle(star, i + 1, selectedRating);
        }
    }

    @FXML
    private void handleSubmitRating() {
        if (selectedRating == 0) {
            new Alert(Alert.AlertType.WARNING, "Please select a star rating.").showAndWait();
            return;
        }

        String sql = "UPDATE user_books SET rating = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selectedRating);
            pstmt.setInt(2, userBookId);
            pstmt.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "Rating submitted successfully!").showAndWait();

            // Close the dialog
            Stage stage = (Stage) starContainer.getScene().getWindow();
            stage.close();

            // Refresh the bookshelf view
            if (bookshelfController != null) {
                bookshelfController.loadUserBooks();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error submitting rating: " + e.getMessage()).showAndWait();
        }
    }
}