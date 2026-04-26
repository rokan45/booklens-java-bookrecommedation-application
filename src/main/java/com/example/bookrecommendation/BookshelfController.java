package com.example.bookrecommendation;

import com.example.bookrecommendation.model.Book;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class BookshelfController implements Initializable {

    @FXML private TilePane wantToReadPane;
    @FXML private TilePane readingPane;
    @FXML private TilePane completedPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadUserBooks();
    }


    public void loadUserBooks() {
        // to prevent duplicates if reloaded
        wantToReadPane.getChildren().clear();
        readingPane.getChildren().clear();
        completedPane.getChildren().clear();

        UserSession session = UserSession.getInstance();
        if (session == null) {
            System.err.println("Bookshelf: No user logged in, cannot load books.");
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bookrecommendation/login-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                Stage stage = (Stage) (wantToReadPane != null ? wantToReadPane.getScene().getWindow() : new Stage());
                stage.setScene(scene);
                stage.setTitle("Book Recommender - Login");
                stage.setMaximized(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        int userId = session.getUserId();

        String sql = "SELECT b.*, ub.id AS user_book_id, ub.status, ub.rating FROM books b " +
                "JOIN user_books ub ON b.id = ub.book_id " +
                "WHERE ub.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Book book = new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getInt("year"),
                        rs.getString("language"),
                        rs.getString("imagepath")
                );

                int userBookId = rs.getInt("user_book_id");
                String status = rs.getString("status");
                int userRating = rs.getInt("rating");

                VBox card = createMiniBookCard(book, userBookId, status, userRating);

                if ("WANT_TO_READ".equals(status)) {
                    wantToReadPane.getChildren().add(card);
                } else if ("READING".equals(status)) {
                    readingPane.getChildren().add(card);
                } else if ("COMPLETED".equals(status)) {
                    completedPane.getChildren().add(card);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private VBox createMiniBookCard(Book book, int userBookId, String currentStatus, int currentRating) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1); -fx-background-radius: 5px;");

        // Image
        ImageView cover = new ImageView();
        try {
            if (getClass().getResource(book.getCoverImagePath()) != null) {
                cover.setImage(new Image(getClass().getResourceAsStream(book.getCoverImagePath())));
            } else {
                cover.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
            }
        } catch (Exception e) {
            cover.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
        }
        cover.setFitHeight(120);
        cover.setFitWidth(80);

        Label title = new Label(book.getTitle());
        title.setWrapText(true);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        title.setMaxWidth(100);

        // --- Common Remove Button ---
        Button removeButton = new Button("Remove");
        removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 3px; -fx-cursor: hand;");
        removeButton.setOnAction(event -> confirmRemoveBook(userBookId, book.getTitle()));

        // Add core elements
        card.getChildren().addAll(cover, title);

        // --- CONDITIONAL UI based on STATUS ---
        HBox actionButtons = new HBox(5);
        actionButtons.setAlignment(Pos.CENTER);
        VBox.setMargin(actionButtons, new Insets(5,0,0,0)); // Margin above action buttons

        if ("WANT_TO_READ".equals(currentStatus)) {
            Button startReadingButton = new Button("Start Reading");
            startReadingButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 3px; -fx-cursor: hand;");
            startReadingButton.setOnAction(event -> updateBookStatus(userBookId, "READING"));
            actionButtons.getChildren().add(startReadingButton);

        } else if ("READING".equals(currentStatus)) {
            Button markCompletedButton = new Button("Mark Completed");
            markCompletedButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 3px; -fx-cursor: hand;");
            markCompletedButton.setOnAction(event -> updateBookStatus(userBookId, "COMPLETED"));
            actionButtons.getChildren().add(markCompletedButton);

        } else if ("COMPLETED".equals(currentStatus)) {
            Button rateButton = new Button("Rate Book");
            rateButton.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 3px; -fx-cursor: hand;");
            rateButton.setOnAction(event -> handleRateBookDialog(book, userBookId, currentRating));
            actionButtons.getChildren().add(rateButton);
        }

        // Add action buttons and then the common remove button
        card.getChildren().addAll(actionButtons, removeButton);

        return card;
    }


    private HBox createStarRatingControl(int userBookId, int currentRating) {
        HBox stars = new HBox(2);
        stars.setAlignment(Pos.CENTER);

        for (int i = 1; i <= 5; i++) {
            Button star = new Button();
            if (i <= currentRating) {
                star.setText("★"); // Filled Star
                star.setStyle("-fx-background-color: transparent; -fx-text-fill: gold; -fx-font-size: 16px; -fx-padding: 0;");
            } else {
                star.setText("☆"); // Empty Star
                star.setStyle("-fx-background-color: transparent; -fx-text-fill: gray; -fx-font-size: 16px; -fx-padding: 0;");
            }
            stars.getChildren().add(star);
        }
        return stars;
    }

    /**
     * Updates the status of a book in the user_books table.
     */
    private void updateBookStatus(int userBookId, String newStatus) {
        String sql;
        if ("COMPLETED".equals(newStatus)) {
            sql = "UPDATE user_books SET status = ? WHERE id = ?";
        } else {
            sql = "UPDATE user_books SET status = ?, rating = NULL WHERE id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, userBookId);
            pstmt.executeUpdate();

            loadUserBooks();

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error updating book status.").showAndWait();
        }
    }

    /**
     * Opens a dialog for the user to rate the book.
     */
    private void handleRateBookDialog(Book book, int userBookId, int currentRating) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bookrecommendation/rating-dialog-view.fxml"));
            VBox root = loader.load();
            RatingDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Rate: " + book.getTitle());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner((Stage) wantToReadPane.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.initData(userBookId, currentRating, this);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not open rating dialog.").showAndWait();
        }
    }


    /**
     * Confirms and removes a book from the user's shelf.
     */
    private void confirmRemoveBook(int userBookId, String bookTitle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Removal");
        alert.setHeaderText("Remove '" + bookTitle + "' from your shelf?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                removeBookFromShelf(userBookId);
            }
        });
    }

    private void removeBookFromShelf(int userBookId) {
        String sql = "DELETE FROM user_books WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userBookId);
            pstmt.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "Book removed from your shelf.").showAndWait();
            loadUserBooks(); // Refresh the view

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error removing book.").showAndWait();
        }
    }

    @FXML
    private void handleBackToHome() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bookrecommendation/homepage-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) wantToReadPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Book Recommender - Home");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}