package com.govscheme.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "aadhaar_number", length = 20)
    private String aadhaarNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private Integer age;

    @Column(length = 20)
    private String gender; // Male, Female, Other, Transgender

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String district;

    @Column(name = "annual_income")
    private Double annualIncome;

    @Column(length = 100)
    private String occupation; // Student, Farmer, Self-Employed, Unemployed, Salaried, Business Owner

    @Column(name = "employment_status", length = 50)
    private String employmentStatus; // Employed, Unemployed, Self-Employed, Student

    @Column(length = 50)
    private String category; // General, OBC, SC, ST, BC, MBC, EWS

    @Column(name = "student_status")
    @Builder.Default
    private Boolean studentStatus = false;

    @Column(name = "disability_status")
    @Builder.Default
    private Boolean disabilityStatus = false;

    @Column(name = "farmer_status")
    @Builder.Default
    private Boolean farmerStatus = false;

    @Column(name = "entrepreneur_status")
    @Builder.Default
    private Boolean entrepreneurStatus = false;

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
