package com.example.bookrecommendation;


import com.example.bookrecommendation.model.Book;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HomePageController implements Initializable {

    //<editor-fold desc="FXML Declarations">
    @FXML private TextField searchBar;
    @FXML private Button searchButton;

    @FXML private Button bookshelfButton;
    @FXML private Button profileButton;

    @FXML private ComboBox<String> genreFilter;
    @FXML private ComboBox<String> authorFilter;
    @FXML private ComboBox<String> languageFilter;
    @FXML private Button applyFiltersButton;
    @FXML private Button resetFiltersButton;

    @FXML private HBox recommendedBooksContainer;
    @FXML private HBox topRatedBooksContainer;
    @FXML private TilePane bookCatalogTilePane;
    //</editor-fold>

    // Master list of all books
    private final List<Book> allBooks = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Load data
        loadAllBooksFromDatabase();

        // 2. Populate Filters
        populateFilters();

        // 3. Load Smart Recommendations
        loadSmartRecommendations();

        // --- FIX: 4. Load Dynamic Top Rated Books ---
        loadTopRatedBooks();

        // 5. Show all books in the main catalog
        displayBooksInCatalog(allBooks);

        // 6. Connect Buttons
        setupEventListeners();
    }

    // --- DATA LOADING ---

    private void loadAllBooksFromDatabase() {
        allBooks.clear();
        String sql = "SELECT id, title, author, genre, year, language, imagepath FROM books";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

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
                allBooks.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error loading books from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateFilters() {
        genreFilter.getItems().clear();
        authorFilter.getItems().clear();
        languageFilter.getItems().clear();

        genreFilter.getItems().add("All Genres");
        authorFilter.getItems().add("All Authors");
        languageFilter.getItems().add("All Languages");

        genreFilter.setValue("All Genres");
        authorFilter.setValue("All Authors");
        languageFilter.setValue("All Languages");

        List<String> genres = allBooks.stream().map(Book::getGenre).distinct().sorted().collect(Collectors.toList());
        genreFilter.getItems().addAll(genres);

        List<String> authors = allBooks.stream().map(Book::getAuthor).distinct().sorted().collect(Collectors.toList());
        authorFilter.getItems().addAll(authors);

        List<String> languages = allBooks.stream().map(Book::getLanguage).distinct().sorted().collect(Collectors.toList());
        languageFilter.getItems().addAll(languages);
    }

    private void loadSmartRecommendations() {
        UserSession session = UserSession.getInstance();
        if (session == null) return;
        int userId = session.getUserId();

        List<Book> recommendedBooks = new ArrayList<>();
        String favoriteGenre = null;

        String genreSql = "SELECT b.genre, COUNT(*) as count FROM user_books ub JOIN books b ON ub.book_id = b.id WHERE ub.user_id = ? GROUP BY b.genre ORDER BY count DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(genreSql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) favoriteGenre = rs.getString("genre");
        } catch (SQLException e) { e.printStackTrace(); }

        String recSql;
        if (favoriteGenre != null) {
            recSql = "SELECT * FROM books WHERE genre = ? AND id NOT IN (SELECT book_id FROM user_books WHERE user_id = ?) LIMIT 5";
        } else {
            recSql = "SELECT * FROM books ORDER BY RAND() LIMIT 5";
        }

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(recSql)) {
            if (favoriteGenre != null) {
                pstmt.setString(1, favoriteGenre);
                pstmt.setInt(2, userId);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                recommendedBooks.add(new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getString("genre"), rs.getInt("year"), rs.getString("language"), rs.getString("imagepath")));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        displayBooksInHorizontalContainer(recommendedBooks, recommendedBooksContainer);
    }

    /**
     * Fetches books with the highest average rating (4 or 5 stars) from all users.
     */
    private void loadTopRatedBooks() {
        List<Book> topRatedBooks = new ArrayList<>();

        String sql = "SELECT b.*, AVG(ub.rating) AS avg_rating " +
                "FROM books b " +
                "JOIN user_books ub ON b.id = ub.book_id " +
                "WHERE ub.rating IS NOT NULL " +
                "GROUP BY b.id, b.title, b.author, b.genre, b.year, b.language, b.imagepath " +
                "HAVING AVG(ub.rating) >= 4.0 " +
                "ORDER BY avg_rating DESC, COUNT(ub.rating) DESC " +
                "LIMIT 5";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

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
                topRatedBooks.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error loading top rated books: " + e.getMessage());
            e.printStackTrace();
        }
        displayBooksInHorizontalContainer(topRatedBooks, topRatedBooksContainer);
    }


    // --- EVENT LISTENERS ---

    private void setupEventListeners() {
        searchBar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleSearch();
        });

        if (searchButton != null) searchButton.setOnAction(event -> handleSearch());
        if (applyFiltersButton != null) applyFiltersButton.setOnAction(event -> handleApplyFiltersButton());
        if (resetFiltersButton != null) resetFiltersButton.setOnAction(event -> handleResetFiltersButton());
        if (bookshelfButton != null) bookshelfButton.setOnAction(event -> handleBookshelfButton());
        if (profileButton != null) profileButton.setOnAction(event -> handleProfileButton());
    }

    // --- LOGIC HANDLERS ---

    private void handleSearch() {
        String query = searchBar.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            return;
        }
        List<Book> results = allBooks.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(query) || b.getAuthor().toLowerCase().contains(query))
                .collect(Collectors.toList());
        openResultsWindow(results, "Search Results for: \"" + searchBar.getText() + "\"");
    }

    private void handleApplyFiltersButton() {
        String g = genreFilter.getValue();
        String a = authorFilter.getValue();
        String l = languageFilter.getValue();

        List<Book> results = allBooks.stream()
                .filter(b -> (g == null || "All Genres".equals(g) || b.getGenre().equalsIgnoreCase(g)))
                .filter(b -> (a == null || "All Authors".equals(a) || b.getAuthor().equalsIgnoreCase(a)))
                .filter(b -> (l == null || "All Languages".equals(l) || b.getLanguage().equalsIgnoreCase(l)))
                .collect(Collectors.toList());
        openResultsWindow(results, "Filtered Results");
    }

    private void handleResetFiltersButton() {
        genreFilter.setValue("All Genres");
        authorFilter.setValue("All Authors");
        languageFilter.setValue("All Languages");
        searchBar.clear();
        displayBooksInCatalog(allBooks);
    }

    private void openResultsWindow(List<Book> books, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bookrecommendation/search-results-view.fxml"));
            Scene scene = new Scene(loader.load());
            SearchResultsController controller = loader.getController();
            controller.setResults(books, title);
            Stage stage = new Stage();
            stage.setTitle("Results");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleBookshelfButton() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bookrecommendation/bookshelf-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) bookshelfButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("My Bookshelf");
            stage.setMaximized(true);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleProfileButton() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bookrecommendation/profile-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) profileButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("My Profile");
            stage.setMaximized(true);
        } catch (IOException e) { e.printStackTrace(); }
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

    // ui

    private void displayBooksInCatalog(List<Book> books) {
        bookCatalogTilePane.getChildren().clear();
        for (Book book : books) bookCatalogTilePane.getChildren().add(createBookCard(book));
    }

    private void displayBooksInHorizontalContainer(List<Book> books, HBox container) {
        container.getChildren().clear();
        for (Book book : books) container.getChildren().add(createBookCard(book));
    }

    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("book-card");

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
        cover.setFitHeight(200);
        cover.setFitWidth(180);
        cover.setPreserveRatio(true);

        Label title = new Label(book.getTitle());
        title.getStyleClass().add("book-title");
        title.setWrapText(true);
        title.setTextAlignment(TextAlignment.CENTER);

        Label author = new Label(book.getAuthor());
        author.getStyleClass().add("book-author");

        Button addButton = new Button("+ Add to Shelf");
        addButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 5 10; -fx-font-size: 12px;");
        addButton.setOnAction(event -> {
            event.consume();
            addBookToShelf(book);
        });

        card.getChildren().addAll(cover, title, author, addButton);
        card.setOnMouseClicked(event -> handleBookClick(book));
        return card;
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