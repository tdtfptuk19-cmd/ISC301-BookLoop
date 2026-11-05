package com.bookloop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

/**
 * Mapping between an academic Course and an AcademicBook.
 */
@Entity
@Table(name = "course_books", uniqueConstraints = {
        @UniqueConstraint(name = "uq_course_book", columnNames = {"course_id", "book_id"})
}, indexes = {
        @Index(name = "idx_cb_course", columnList = "course_id"),
        @Index(name = "idx_cb_book", columnList = "book_id")
})
public class CourseBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private AcademicBook book;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 30)
    private CourseBookRelationshipType relationshipType = CourseBookRelationshipType.SUGGESTED_REFERENCE;

    @Size(max = 1000)
    @Column(name = "notes", columnDefinition = "NVARCHAR(1000)")
    private String notes;

    @Size(max = 500)
    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "is_official", nullable = false)
    private boolean official = false;

    public CourseBook() {}

    public CourseBook(Course course, AcademicBook book, CourseBookRelationshipType relationshipType, String notes, String sourceUrl, boolean official) {
        this.course = course;
        this.book = book;
        this.relationshipType = relationshipType;
        this.notes = notes;
        this.sourceUrl = sourceUrl;
        this.official = official;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public AcademicBook getBook() { return book; }
    public void setBook(AcademicBook book) { this.book = book; }

    public CourseBookRelationshipType getRelationshipType() { return relationshipType; }
    public void setRelationshipType(CourseBookRelationshipType relationshipType) { this.relationshipType = relationshipType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public boolean isOfficial() { return official; }
    public void setOfficial(boolean official) { this.official = official; }
}

