package com.govscheme.repository;

import com.govscheme.entity.EligibilityRule;
import com.govscheme.entity.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EligibilityRuleRepository extends JpaRepository<EligibilityRule, Long> {
    Optional<EligibilityRule> findByScheme(Scheme scheme);
    Optional<EligibilityRule> findBySchemeId(Long schemeId);
}
