package com.bookloop.repository;

import com.bookloop.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {
    List<Campus> findByInstitutionIdOrderByNameAsc(Long institutionId);
    Optional<Campus> findByInstitutionIdAndNameIgnoreCase(Long institutionId, String name);
}

