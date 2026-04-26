package com.example.bookrecommendation;


import com.example.bookrecommendation.model.Book;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookDetailsController {

    @FXML private ImageView coverImage;
    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label genreLabel;
    @FXML private Label yearLabel;
    @FXML private Label ratingLabel;
    @FXML private Label languageLabel;
    @FXML private Button addToShelfButton;

    private Book currentBook;

    /**
     * This method is called by the HomePageController to pass the book data.
     */
    public void setBookData(Book book) {
        this.currentBook = book;

        // 1. Set Text Data
        titleLabel.setText(book.getTitle());
        authorLabel.setText("by " + book.getAuthor());
        genreLabel.setText(book.getGenre());
        yearLabel.setText(String.valueOf(book.getPublishedYear()));
        languageLabel.setText("Language: " + book.getLanguage());

        // 2. Set Image safely
        try {
            if (getClass().getResource(book.getCoverImagePath()) != null) {
                coverImage.setImage(new Image(getClass().getResourceAsStream(book.getCoverImagePath())));
            } else {
                coverImage.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
            }
        } catch (Exception e) {
            coverImage.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
        }

        // 3. Load Dynamic Data (Average Rating)
        loadAverageRating();

        // 4. Setup Button Action
        addToShelfButton.setOnAction(e -> handleAddToShelf());
    }

    private void loadAverageRating() {
        String sql = "SELECT AVG(rating) as avg_rating, COUNT(*) as count FROM user_books WHERE book_id = ? AND rating IS NOT NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentBook.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double avg = rs.getDouble("avg_rating");
                int count = rs.getInt("count");

                if (count > 0) {
                    ratingLabel.setText(String.format("⭐ %.1f / 5 (Based on %d ratings)", avg, count));
                } else {
                    ratingLabel.setText("No ratings yet");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleAddToShelf() {
        // Reuse logic similar to HomePage, but simplified
        UserSession session = UserSession.getInstance();
        if (session == null) return;

        String sql = "INSERT INTO user_books (user_id, book_id, status) VALUES (?, ?, 'WANT_TO_READ')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, session.getUserId());
            pstmt.setInt(2, currentBook.getId());
            pstmt.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Added to shelf!");
            alert.showAndWait();

            // Close the popup after adding
            Stage stage = (Stage) addToShelfButton.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("You already have this book.");
                alert.showAndWait();
            }
        }
    }
}