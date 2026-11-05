package com.bookloop.repository;

import com.bookloop.entity.AcademicProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicProgramRepository extends JpaRepository<AcademicProgram, Long> {
    List<AcademicProgram> findByInstitutionIdOrderByNameAsc(Long institutionId);
    List<AcademicProgram> findByCampusIdOrderByNameAsc(Long campusId);
    Optional<AcademicProgram> findByInstitutionIdAndNameIgnoreCase(Long institutionId, String name);
}

