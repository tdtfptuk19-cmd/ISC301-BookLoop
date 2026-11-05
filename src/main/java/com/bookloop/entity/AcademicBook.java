package com.bookloop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Curated textbook or academic reference metadata (not tied to specific inventory).
 */
@Entity
@Table(name = "academic_books", indexes = {
        @Index(name = "idx_acad_book_isbn", columnList = "isbn"),
        @Index(name = "idx_acad_book_title", columnList = "title")
})
public class AcademicBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 30)
    @Column(name = "isbn", length = 30)
    private String isbn;

    @NotBlank
    @Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Size(max = 255)
    @Column(name = "original_title", length = 255)
    private String originalTitle;

    @Size(max = 200)
    @Column(name = "author", length = 200)
    private String author;

    @Size(max = 150)
    @Column(name = "publisher", length = 150)
    private String publisher;

    @Size(max = 50)
    @Column(name = "edition", length = 50)
    private String edition;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Size(max = 50)
    @Column(name = "language", length = 50)
    private String language; // e.g., "Tiếng Việt", "English"

    @Size(max = 3000)
    @Column(name = "description", columnDefinition = "NVARCHAR(3000)")
    private String description;

    @Size(max = 500)
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Size(max = 100)
    @Column(name = "category", length = 100)
    private String category; // e.g., "Công nghệ thông tin", "Kinh tế - Tài chính"

    @Size(max = 50)
    @Column(name = "book_type", length = 50)
    private String bookType; // "Giáo trình chính", "Sách bài tập", "Sách chuyên khảo"

    public AcademicBook() {}

    public AcademicBook(String isbn, String title, String originalTitle, String author, String publisher, String edition, Integer publicationYear, String language, String description, String coverImageUrl, String category, String bookType) {
        this.isbn = isbn;
        this.title = title;
        this.originalTitle = originalTitle;
        this.author = author;
        this.publisher = publisher;
        this.edition = edition;
        this.publicationYear = publicationYear;
        this.language = language;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.category = category;
        this.bookType = bookType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOriginalTitle() { return originalTitle; }
    public void setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBookType() { return bookType; }
    public void setBookType(String bookType) { this.bookType = bookType; }
}

