package com.govscheme.repository;

import com.govscheme.entity.Scheme;
import com.govscheme.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchemeRepository extends JpaRepository<Scheme, Long> {
    List<Scheme> findByStatus(Status status);
    List<Scheme> findByCategoryIdAndStatus(Long categoryId, Status status);
    List<Scheme> findBySchemeTypeAndStatus(String schemeType, Status status);

    @Query("SELECT s FROM Scheme s WHERE s.status = :status AND " +
           "(LOWER(s.schemeName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.shortDescription) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.department) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.category.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Scheme> searchSchemes(@Param("query") String query, @Param("status") Status status);

    @Query("SELECT s FROM Scheme s JOIN s.eligibilityRule r WHERE s.status = 'ACTIVE'")
    List<Scheme> findAllActiveWithRules();
}
