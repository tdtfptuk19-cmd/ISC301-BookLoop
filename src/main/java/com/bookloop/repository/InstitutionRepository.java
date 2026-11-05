package com.bookloop.repository;

import com.bookloop.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByShortNameIgnoreCase(String shortName);
    List<Institution> findAllByActiveTrueOrderByNameAsc();
}

