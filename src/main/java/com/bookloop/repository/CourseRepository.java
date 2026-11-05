package com.bookloop.repository;

import com.bookloop.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByProgramIdOrderBySemesterAscNameAsc(Long programId);
    Optional<Course> findByProgramIdAndCourseCodeIgnoreCase(Long programId, String courseCode);

    @Query("""
            SELECT c FROM Course c LEFT JOIN FETCH c.program p LEFT JOIN FETCH p.institution
            WHERE (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:institutionId IS NULL OR p.institution.id = :institutionId)
              AND (:programId IS NULL OR p.id = :programId)
            ORDER BY c.name ASC
            """)
    Page<Course> searchCourses(@Param("keyword") String keyword,
                               @Param("institutionId") Long institutionId,
                               @Param("programId") Long programId,
                               Pageable pageable);
}

