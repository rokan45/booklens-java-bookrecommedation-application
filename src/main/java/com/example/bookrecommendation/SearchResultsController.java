package com.example.bookrecommendation;


import com.example.bookrecommendation.model.Book;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SearchResultsController {

    @FXML private Label headerLabel;
    @FXML private TilePane resultsTilePane;

    /**
     * Receives the list of books and the title (e.g., "Search Results for 'Harry'")
     */
    public void setResults(List<Book> books, String titleText) {
        headerLabel.setText(titleText);
        resultsTilePane.getChildren().clear();

        if (books.isEmpty()) {
            Label noData = new Label("No books found matching your criteria.");
            noData.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");
            resultsTilePane.getChildren().add(noData);
        } else {
            for (Book book : books) {
                resultsTilePane.getChildren().add(createBookCard(book));
            }
        }
    }

    // --- REUSED LOGIC FOR CREATING BOOK CARDS ---
    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("book-card");
        // Inline style in case CSS isn't loaded for the popup
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1); -fx-padding: 10;");

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
        cover.setFitHeight(180);
        cover.setPreserveRatio(true);

        Label title = new Label(book.getTitle());
        title.setWrapText(true);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label author = new Label(book.getAuthor());
        author.setStyle("-fx-text-fill: #7f8c8d;");

        Button addButton = new Button("+ Add to Shelf");
        addButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        addButton.setOnAction(event -> {
            event.consume();
            addBookToShelf(book);
        });

        card.getChildren().addAll(cover, title, author, addButton);
        card.setOnMouseClicked(event -> handleBookClick(book));
        return card;
    }

    private void handleBookClick(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bookrecommendation/book-details-view.fxml"));
            Scene scene = new Scene(loader.load());
            BookDetailsController controller = loader.getController();
            controller.setBookData(book);
            Stage stage = new Stage();
            stage.setTitle(book.getTitle());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void addBookToShelf(Book book) {
        UserSession session = UserSession.getInstance();
        if (session == null) return;
        String sql = "INSERT INTO user_books (user_id, book_id, status) VALUES (?, ?, 'WANT_TO_READ')";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, session.getUserId());
            pstmt.setInt(2, book.getId());
            pstmt.executeUpdate();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Added '" + book.getTitle() + "' to your Bookshelf!");
            alert.showAndWait();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("This book is already on your shelf.");
                alert.showAndWait();
            }
        }
    }
}