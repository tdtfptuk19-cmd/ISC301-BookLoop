package com.bookloop.dto;

import com.bookloop.entity.Condition;
import com.bookloop.entity.ListingStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Form object for creating or editing a book listing.
 * The seller is derived from the authenticated user in the service layer —
 * it is never trusted from a browser-submitted field.
 */
public class ProductForm {

    @NotBlank(message = "Book title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 3000)
    private String description;

    @NotNull(message = "Vui lòng nhập giá bán")
    @DecimalMin(value = "0.00", inclusive = true, message = "Giá bán không được âm")
    @Digits(integer = 10, fraction = 2, message = "Giá bán phải là số hợp lệ (VNĐ)")
    private BigDecimal price;

    @NotNull(message = "Please select the book condition")
    private Condition condition;

    @Size(max = 100)
    private String category;

    private Long categoryId;

    @Size(max = 150)
    private String author;

    @Size(max = 150)
    private String publisher;

    @Size(max = 50)
    private String edition;

    @Size(max = 20)
    private String isbn;

    @Size(max = 20)
    private String courseCode;

    private String imageUrl;

    @Size(max = 1000)
    private String defectNotes;

    @NotBlank(message = "Campus / meetup location is required")
    @Size(max = 100)
    private String campus;

    private ListingStatus status = ListingStatus.AVAILABLE;

    private Long academicBookId;

    // --- Getters and Setters ---

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
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public Long getAcademicBookId() {
        return academicBookId;
    }

    public void setAcademicBookId(Long academicBookId) {
        this.academicBookId = academicBookId;
    }
}
