package com.bookloop.repository;

import com.bookloop.entity.Role;
import com.bookloop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity persistence operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email address (case-insensitive lookup).
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Checks if an email address is already registered.
     */
    boolean existsByEmailIgnoreCase(String email);

    long countByEnabled(boolean enabled);

    long countByRole(Role role);

    /**
     * Count active ADMIN users (for last-admin protection).
     */
    long countByRoleAndEnabled(Role role, boolean enabled);

    /**
     * Admin search: filter by keyword (name/email), role, and enabled status.
     */
    @Query("""
            SELECT u FROM User u
            WHERE (:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:role IS NULL OR u.role = :role)
              AND (:enabled IS NULL OR u.enabled = :enabled)
            ORDER BY u.createdAt DESC
            """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") Role role,
            @Param("enabled") Boolean enabled,
            Pageable pageable);

    /**
     * Recent registrations for dashboard.
     */
    List<User> findTop5ByOrderByCreatedAtDesc();
}
