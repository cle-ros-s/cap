package com.govscheme.repository;

import com.govscheme.entity.ApplicationProcedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationProcedureRepository extends JpaRepository<ApplicationProcedure, Long> {
    List<ApplicationProcedure> findBySchemeIdOrderByStepNumberAsc(Long schemeId);
    void deleteBySchemeId(Long schemeId);
}
