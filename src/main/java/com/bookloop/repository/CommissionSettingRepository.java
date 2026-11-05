package com.bookloop.repository;

import com.bookloop.entity.CommissionSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CommissionSettingRepository extends JpaRepository<CommissionSetting, Long> {

    @Query("SELECT c FROM CommissionSetting c WHERE c.active = true AND c.effectiveFrom <= :now AND (c.effectiveTo IS NULL OR c.effectiveTo > :now) ORDER BY c.effectiveFrom DESC")
    Optional<CommissionSetting> findActiveSetting(@Param("now") LocalDateTime now);

    Optional<CommissionSetting> findTopByActiveTrueOrderByEffectiveFromDesc();
}

