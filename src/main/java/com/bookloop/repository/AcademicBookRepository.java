package com.bookloop.repository;

import com.bookloop.entity.AcademicBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicBookRepository extends JpaRepository<AcademicBook, Long> {
    Optional<AcademicBook> findByTitleIgnoreCase(String title);
    Optional<AcademicBook> findByIsbn(String isbn);

    @Query("""
            SELECT b FROM AcademicBook b
            WHERE (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:category IS NULL OR b.category = :category)
            ORDER BY b.title ASC
            """)
    Page<AcademicBook> searchAcademicBooks(@Param("keyword") String keyword,
                                          @Param("category") String category,
                                          Pageable pageable);

    @Query(value = """
            SELECT DISTINCT b FROM AcademicBook b
            LEFT JOIN CourseBook cb ON cb.book = b
            LEFT JOIN cb.course c
            LEFT JOIN c.program p
            LEFT JOIN p.institution inst
            LEFT JOIN p.campus camp
            WHERE (:institutionId IS NULL OR inst.id = :institutionId)
              AND (:campusId IS NULL OR camp.id = :campusId)
              AND (:programId IS NULL OR p.id = :programId)
              AND (:courseId IS NULL OR c.id = :courseId)
              AND (:category IS NULL OR b.category = :category)
              AND (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY b.title ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT b) FROM AcademicBook b
            LEFT JOIN CourseBook cb ON cb.book = b
            LEFT JOIN cb.course c
            LEFT JOIN c.program p
            LEFT JOIN p.institution inst
            LEFT JOIN p.campus camp
            WHERE (:institutionId IS NULL OR inst.id = :institutionId)
              AND (:campusId IS NULL OR camp.id = :campusId)
              AND (:programId IS NULL OR p.id = :programId)
              AND (:courseId IS NULL OR c.id = :courseId)
              AND (:category IS NULL OR b.category = :category)
              AND (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<AcademicBook> searchWithHierarchy(
            @Param("institutionId") Long institutionId,
            @Param("campusId") Long campusId,
            @Param("programId") Long programId,
            @Param("courseId") Long courseId,
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT DISTINCT b.category FROM AcademicBook b WHERE b.category IS NOT NULL ORDER BY b.category")
    List<String> findDistinctCategories();
}

