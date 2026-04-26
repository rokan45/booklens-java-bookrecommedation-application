# BookLens: A Personalized Desktop Book Recommendation System
(This application is built for academic purposes only, and there is a lot of scope to work to meet commercial purposes.)

![JavaFX](https://img.shields.io/badge/JavaFX-17%2B-blue?logo=javafx&logoColor=white)
![Java](https://img.shields.io/badge/Java-17%2B-red?logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-orange?logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-orange?logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)

## 📝 Project Description

BookLens is an intelligent desktop application designed to discover and manage books based on genre. In an era of vast digital libraries, readers often face "choice overload" and struggle to find what interests them. BookLens addresses this by providing a personalized recommendation engine based on favorite genres, a dynamic book catalog, and personalized bookshelves along with search and filter functionality.

Developed using JavaFX to create an engaging user interface and supported by a MySQL database for reliable data storage, BookLens offers a smooth and enjoyable experience for all book enthusiasts.

## ✨ Key Features

*   **Secure User Authentication:** Robust user registration and login system with password hashing for data security.
*   **Dynamic Book Catalog:** Browse a comprehensive catalog of books fetched dynamically from the database, complete with cover images, titles, and authors.
*   **Personalized Bookshelf Management:** Organize your reading journey by categorizing books into "Want to Read," "Currently Reading," and "Completed" shelves.
*   **Interactive Rating System:** Rate completed books on a 1-5 star scale via a dedicated dialog, influencing future recommendations.
*   **Intelligent Recommendation Engine:** Get personalized book suggestions based on your demonstrated reading preferences (favorite genres), ensuring you always find something new and exciting to read (includes a cold-start fallback for new users).
*   **Comprehensive Search & Filtering:** Easily find books by title or author, and refine results using filters for genre, author, and language. Search/filter results are displayed in a separate window for an enhanced user experience.
*   **Detailed Book Information:** View in-depth details about any book through an interactive pop-up, including title, author, genre, year, language, and average user ratings.
*   **User Profile & Statistics:** A personal dashboard displaying aggregated reading statistics, such as total books on shelf, completed books, average rating given, and favorite genre.
*   **Responsive UI:** Designed for an aesthetically pleasing and user-friendly experience, with windows launching maximized.

## 🚀 Technology Stack

*   **Frontend Framework:** JavaFX
*   **Programming Language:** Java 17+
*   **IDE:** IntelliJ IDEA (Recommended)
*   **Database:** MySQL 8.0+
*   **Database Connectivity:** JDBC (Java Database Connectivity)
*   **Build Tool:** Apache Maven

## 📋 Setup & Installation

Follow these steps to get BookLens up and running on your local machine.

### Prerequisites

*   **Java Development Kit (JDK) 17+:** Ensure `JAVA_HOME` is set.
*   **MySQL Server 8.0+:** Running locally (or access to a remote instance).
*   **Apache Maven:** Installed and configured.
*   **IntelliJ IDEA:** (Recommended IDE for easy setup).

### 1. Clone the Repository

```bash
git clone https://github.com/rokan45/booklens-java-bookrecommedation-application

```
### 2. Database Setup

## i. Create Database:

```
CREATE DATABASE bookapp;
USE bookapp;
```

## ii. Create Tables:
```
-- Create 'users' table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

-- Create 'books' table
CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    published_year INT,
    language VARCHAR(50),
    country VARCHAR(100),
    imagepath VARCHAR(255) -- Stores relative path: /images/Book Title.jpeg
);

-- Create 'user_books' table (for user's bookshelf and ratings)
CREATE TABLE user_books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'WANT_TO_READ', -- 'WANT_TO_READ', 'READING', 'COMPLETED'
    rating INT DEFAULT NULL, -- A rating from 1 to 5
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    UNIQUE KEY user_book_unique (user_id, book_id)
);

```
## iii. Configure DatabaseConnection.java:
```
// Example:
private static final String URL = "jdbc:mysql://localhost:3306/bookapp";
private static final String USER = "your_mysql_username";
private static final String PASSWORD = "your_mysql_password";
```
### 3.IDE Setup (IntelliJ IDEA)
```
Open Project: Open the BookLens project in IntelliJ IDEA.
Maven Reload: IntelliJ should automatically detect the pom.xml and prompt to load Maven dependencies. If not, click the Maven icon on the right sidebar and click "Reload All Maven Projects".
Configure JavaFX VM Options:
Go to Run -> Edit Configurations...
Find your Main run configuration.
In the "VM options" field, add the following (replace PATH_TO_JAVAFX_SDK with the actual path to your downloaded JavaFX SDK's lib directory):
code
Code
--module-path "PATH_TO_JAVAFX_SDK/lib" --add-modules javafx.controls,javafx.fxml
Example (Windows): --module-path "C:\path\to\javafx-sdk-17\lib" --add-modules javafx.controls,javafx.fxml
Example (macOS/Linux): --module-path /path/to/javafx-sdk-17/lib --add-modules javafx.controls,javafx.fxml
Check module-info.java: Ensure your src/main/java/module-info.java file contains the necessary requires and opens statements:
code
Java
module com.example.bookrecommendation {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j; // For MySQL JDBC driver

    opens com.example.bookrecommendation to javafx.fxml;
    exports com.example.bookrecommendation;
    exports com.example.bookrecommendation.model; // Export models
    exports com.example.bookrecommendation.db; // Export DB classes if needed
}
```
### 4.Run the Application
```
Navigate to src/main/java/com/example/bookrecommendation/Main.java.
Right-click and select "Run 'Main.main()'".
The application should launch with the login screen.
```

### 🤝 Contributing
Contributions are welcome! If you have suggestions or find issues, please open an issue or submit a pull request.

