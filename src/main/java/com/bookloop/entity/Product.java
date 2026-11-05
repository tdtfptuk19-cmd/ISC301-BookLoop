package com.bookloop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a textbook listing posted by a student seller.
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_status", columnList = "status"),
        @Index(name = "idx_product_seller", columnList = "seller_id"),
        @Index(name = "idx_product_category", columnList = "category"),
        @Index(name = "idx_product_campus", columnList = "campus")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Book title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(name = "title", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Size(max = 3000, message = "Description cannot exceed 3000 characters")
    @Column(name = "description", columnDefinition = "NVARCHAR(3000)")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Price must be a valid monetary amount")
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Condition is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_grade", nullable = false, length = 20)
    private Condition condition;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    @Column(name = "category", length = 100, columnDefinition = "NVARCHAR(100)")
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category categoryRef;

    @Size(max = 150, message = "Author cannot exceed 150 characters")
    @Column(name = "author", length = 150, columnDefinition = "NVARCHAR(150)")
    private String author;

    @Size(max = 150, message = "Publisher cannot exceed 150 characters")
    @Column(name = "publisher", length = 150, columnDefinition = "NVARCHAR(150)")
    private String publisher;

    @Size(max = 50, message = "Edition cannot exceed 50 characters")
    @Column(name = "edition", length = 50)
    private String edition;

    @Size(max = 20, message = "ISBN cannot exceed 20 characters")
    @Column(name = "isbn", length = 20)
    private String isbn;

    @Size(max = 20, message = "Course code cannot exceed 20 characters")
    @Column(name = "course_code", length = 20)
    private String courseCode;

    @Column(name = "image_url")
    private String imageUrl;

    @Size(max = 1000)
    @Column(name = "defect_notes", columnDefinition = "NVARCHAR(1000)")
    private String defectNotes;

    @NotBlank(message = "Campus/meetup location is required")
    @Size(max = 100)
    @Column(name = "campus", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String campus;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ListingStatus status = ListingStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Admin-provided note when hiding a listing (e.g., policy violation reason).
     * Not shown to regular users.
     */
    @Column(name = "moderation_note", columnDefinition = "NVARCHAR(1000)")
    private String moderationNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    /**
     * Optional link to curated academic textbook catalog.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_book_id", nullable = true)
    private AcademicBook academicBook;

    /**
     * Whether this listing has active paid promotion.
     */
    @Column(name = "is_promoted", nullable = false, columnDefinition = "BIT NOT NULL DEFAULT 0")
    private boolean promoted = false;

    /**
     * Expiration timestamp for the promotion.
     */
    @Column(name = "promoted_until")
    private LocalDateTime promotedUntil;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Constructors ---

    public Product() {
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public String getCategory() {
        if (category != null && !category.isBlank()) {
            return category;
        }
        return categoryRef != null ? categoryRef.getName() : null;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Category getCategoryRef() {
        return categoryRef;
    }

    public void setCategoryRef(Category categoryRef) {
        this.categoryRef = categoryRef;
        if (categoryRef != null && (this.category == null || this.category.isBlank())) {
            this.category = categoryRef.getName();
        }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDefectNotes() {
        return defectNotes;
    }

    public void setDefectNotes(String defectNotes) {
        this.defectNotes = defectNotes;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public String getModerationNote() {
        return moderationNote;
    }

    public void setModerationNote(String moderationNote) {
        this.moderationNote = moderationNote;
    }

    public AcademicBook getAcademicBook() {
        return academicBook;
    }

    public void setAcademicBook(AcademicBook academicBook) {
        this.academicBook = academicBook;
    }

    public boolean isPromoted() {
        return promoted;
    }

    public void setPromoted(boolean promoted) {
        this.promoted = promoted;
    }

    public LocalDateTime getPromotedUntil() {
        return promotedUntil;
    }

    public void setPromotedUntil(LocalDateTime promotedUntil) {
        this.promotedUntil = promotedUntil;
    }

    public boolean isPromotionActive() {
        return promoted && promotedUntil != null && promotedUntil.isAfter(LocalDateTime.now());
    }
}
