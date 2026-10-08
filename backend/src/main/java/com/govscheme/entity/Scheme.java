package com.govscheme.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schemes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private SchemeCategory category;

    @Column(name = "scheme_name", nullable = false, unique = true, length = 255)
    private String schemeName;

    @Column(name = "short_description", nullable = false, columnDefinition = "TEXT")
    private String shortDescription;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(nullable = false, length = 150)
    private String department;

    @Column(name = "scheme_type", nullable = false, length = 50)
    @Builder.Default
    private String schemeType = "CENTRAL"; // CENTRAL / STATE

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String benefits;

    @Column(name = "application_url", length = 500)
    private String applicationUrl;

    @Column(name = "contact_information", columnDefinition = "TEXT")
    private String contactInformation;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @OneToOne(mappedBy = "scheme", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private EligibilityRule eligibilityRule;

    @OneToMany(mappedBy = "scheme", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<RequiredDocument> requiredDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "scheme", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<ApplicationProcedure> applicationProcedures = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
