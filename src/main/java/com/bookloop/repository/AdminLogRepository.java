package com.bookloop.repository;

import com.bookloop.entity.AdminLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {

    @Query("SELECT al FROM AdminLog al LEFT JOIN FETCH al.admin ORDER BY al.createdAt DESC")
    List<AdminLog> findTop20ByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT al FROM AdminLog al LEFT JOIN FETCH al.admin ORDER BY al.createdAt DESC")
    Page<AdminLog> findAllOrderByCreatedAtDesc(Pageable pageable);
}

