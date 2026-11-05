package com.bookloop.repository;

import com.bookloop.entity.CourseBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseBookRepository extends JpaRepository<CourseBook, Long> {
    @Query("SELECT cb FROM CourseBook cb LEFT JOIN FETCH cb.book WHERE cb.course.id = :courseId")
    List<CourseBook> findByCourseIdWithBook(@Param("courseId") Long courseId);

    @Query("SELECT cb FROM CourseBook cb LEFT JOIN FETCH cb.course c LEFT JOIN FETCH c.program p LEFT JOIN FETCH p.institution WHERE cb.book.id = :bookId")
    List<CourseBook> findByBookIdWithCourseDetails(@Param("bookId") Long bookId);

    Optional<CourseBook> findByCourseIdAndBookId(Long courseId, Long bookId);
}

