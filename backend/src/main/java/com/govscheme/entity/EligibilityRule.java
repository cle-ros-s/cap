package com.govscheme.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eligibility_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false, unique = true)
    private Scheme scheme;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "min_income")
    private Double minIncome;

    @Column(name = "max_income")
    private Double maxIncome;

    @Column(length = 20)
    @Builder.Default
    private String gender = "ALL";

    @Column(length = 100)
    @Builder.Default
    private String state = "ALL";

    @Column(length = 100)
    @Builder.Default
    private String district = "ALL";

    @Column(length = 50)
    @Builder.Default
    private String category = "ALL";

    @Column(length = 100)
    @Builder.Default
    private String occupation = "ALL";

    @Column(name = "employment_status", length = 50)
    @Builder.Default
    private String employmentStatus = "ALL";

    @Column(name = "student_status")
    private Boolean studentStatus; // null = any, true = mandatory student, false = mandatory non-student

    @Column(name = "disability_status")
    private Boolean disabilityStatus; // null = any, true = mandatory disabled

    @Column(name = "farmer_status")
    private Boolean farmerStatus; // null = any, true = mandatory farmer

    @Column(name = "entrepreneur_status")
    private Boolean entrepreneurStatus; // null = any, true = mandatory entrepreneur

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
