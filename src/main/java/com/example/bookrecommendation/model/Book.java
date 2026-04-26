package com.example.bookrecommendation.model;

/**
 * This class represents the data model for a single Book.
 * It's a simple Plain Old Java Object (POJO) used to hold book information
 * retrieved from the database.
 */
public class Book {

    // --- FIELDS ---
    // These variables hold the data for a book. They are private to protect the data.
    private final int id;
    private final String title;
    private final String author;
    private final String genre;
    private final int publishedYear;
    private final String language;
    private final String coverImagePath; // e.g., "/images/dune.jpg"

    // --- CONSTRUCTOR ---
    // This is used to create a new Book object, typically after fetching data from the database.
    public Book(int id, String title, String author, String genre, int publishedYear, String language, String coverImagePath) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publishedYear = publishedYear;
        this.language = language;
        this.coverImagePath = coverImagePath;
    }

    // --- GETTERS ---
    // These public methods allow other parts of the application (like the HomePageController)
    // to safely read the book's data without being able to change it.
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public int getPublishedYear() {
        return publishedYear;
    }

    public String getLanguage() {
        return language;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }
}