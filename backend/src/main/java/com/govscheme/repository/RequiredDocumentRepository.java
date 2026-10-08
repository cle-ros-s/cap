package com.govscheme.repository;

import com.govscheme.entity.RequiredDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequiredDocumentRepository extends JpaRepository<RequiredDocument, Long> {
    List<RequiredDocument> findBySchemeId(Long schemeId);
    void deleteBySchemeId(Long schemeId);
}
