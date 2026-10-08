package com.govscheme.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "required_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequiredDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;

    @Column(name = "document_name", nullable = false, length = 150)
    private String documentName;

    @Column(length = 255)
    private String description;

    @Builder.Default
    private Boolean mandatory = true;
}
