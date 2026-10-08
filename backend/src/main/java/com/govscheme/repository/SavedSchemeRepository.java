package com.govscheme.repository;

import com.govscheme.entity.SavedScheme;
import com.govscheme.entity.User;
import com.govscheme.entity.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedSchemeRepository extends JpaRepository<SavedScheme, Long> {
    List<SavedScheme> findByUserId(Long userId);
    Optional<SavedScheme> findByUserIdAndSchemeId(Long userId, Long schemeId);
    Boolean existsByUserIdAndSchemeId(Long userId, Long schemeId);
    void deleteByUserIdAndSchemeId(Long userId, Long schemeId);
    long countByUserId(Long userId);
}
