package com.govscheme.repository;

import com.govscheme.entity.SchemeCategory;
import com.govscheme.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchemeCategoryRepository extends JpaRepository<SchemeCategory, Long> {
    Optional<SchemeCategory> findByName(String name);
    List<SchemeCategory> findByStatus(Status status);
}
