package com.govscheme.service;

import com.govscheme.entity.*;
import com.govscheme.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializerService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository profileRepository;

    @Autowired
    private SchemeCategoryRepository categoryRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private EligibilityRuleRepository ruleRepository;

    @Autowired
    private RequiredDocumentRepository documentRepository;

    @Autowired
    private ApplicationProcedureRepository procedureRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            seedUsers();
        }

        if (categoryRepository.count() == 0) {
            seedCategoriesAndSchemes();
        }
    }

    private void seedUsers() {
        // 1. Admin User
        User admin = User.builder()
                .name("System Administrator")
                .email("admin@gov.in")
                .mobile("9876543210")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ROLE_ADMIN)
                .status(Status.ACTIVE)
                .build();
        userRepository.save(admin);

        // 2. Demo Citizen User (Tamil Nadu Student Profile for testing scenario)
        User citizen = User.builder()
                .name("Aditya Sharma")
                .email("user@gov.in")
                .mobile("9123456789")
                .password(passwordEncoder.encode("user123"))
                .role(Role.ROLE_USER)
                .status(Status.ACTIVE)
                .build();
        citizen = userRepository.save(citizen);

        UserProfile citizenProfile = UserProfile.builder()
                .user(citizen)
                .dateOfBirth(LocalDate.of(2003, 5, 14))
                .age(22)
                .gender("Male")
                .state("Tamil Nadu")
                .district("Chennai")
                .annualIncome(180000.0)
                .occupation("Student")
                .employmentStatus("Student")
                .category("BC")
                .studentStatus(true)
                .disabilityStatus(false)
                .farmerStatus(false)
                .entrepreneurStatus(false)
                .build();
        profileRepository.save(citizenProfile);

        System.out.println(">>> Seeded Initial Accounts: admin@gov.in / admin123, user@gov.in / user123");
    }

    private void seedCategoriesAndSchemes() {
        // Category 1: Education & Student Welfare
        SchemeCategory catEdu = categoryRepository.save(SchemeCategory.builder()
                .name("Education & Student Welfare")
                .description("Scholarships, financial stipends, and educational grants for students.")
                .iconName("bi-mortarboard-fill")
                .status(Status.ACTIVE)
                .build());

        // Category 2: Agriculture & Farming
        SchemeCategory catAgri = categoryRepository.save(SchemeCategory.builder()
                .name("Agriculture & Farming")
                .description("Financial assistance, crop insurance, equipment subsidies for farmers.")
                .iconName("bi-flower1")
                .status(Status.ACTIVE)
                .build());

        // Category 3: Entrepreneurship & Skill Development
        SchemeCategory catEnt = categoryRepository.save(SchemeCategory.builder()
                .name("Entrepreneurship & Skill Development")
                .description("Collateral-free loans, startup incubation grants, and vocational training.")
                .iconName("bi-briefcase-fill")
                .status(Status.ACTIVE)
                .build());

        // Category 4: Healthcare & Wellness
        SchemeCategory catHealth = categoryRepository.save(SchemeCategory.builder()
                .name("Healthcare & Wellness")
                .description("Universal health insurance, free treatment allowances, and medical aid.")
                .iconName("bi-heart-pulse-fill")
                .status(Status.ACTIVE)
                .build());

        // Category 5: Social Welfare & Financial Assistance
        SchemeCategory catSocial = categoryRepository.save(SchemeCategory.builder()
                .name("Social Welfare & Financial Assistance")
                .description("Pension, housing subsidies, disability stipends, and poverty alleviation.")
                .iconName("bi-people-fill")
                .status(Status.ACTIVE)
                .build());

        // --- SCHEME 1: National Higher Education Merit Scholarship ---
        Scheme s1 = schemeRepository.save(Scheme.builder()
                .category(catEdu)
                .schemeName("National Higher Education Merit Scholarship")
                .shortDescription("Financial stipend of ₹20,000/year for underprivileged students pursuing higher education.")
                .description("A comprehensive merit-cum-means scholarship scheme designed to encourage talented students from low-income families to pursue undergraduate and postgraduate education without financial hardship.")
                .department("Department of Higher Education")
                .schemeType("CENTRAL")
                .benefits("₹20,000 annual direct cash transfer to bank account + Tuition Fee Waiver up to ₹50,000.")
                .applicationUrl("https://scholarships.gov.in")
                .contactInformation("Toll Free Helpline: 1800-111-222 | Email: support-nsp@gov.in")
                .status(Status.ACTIVE)
                .build());

        ruleRepository.save(EligibilityRule.builder()
                .scheme(s1)
                .minAge(17)
                .maxAge(25)
                .maxIncome(250000.0)
                .gender("ALL")
                .state("ALL")
                .occupation("Student")
                .studentStatus(true)
                .build());

        documentRepository.saveAll(Arrays.asList(
                RequiredDocument.builder().scheme(s1).documentName("Income Certificate").description("Issued by Tehsildar (valid for current FY)").mandatory(true).build(),
                RequiredDocument.builder().scheme(s1).documentName("Aadhaar Card").description("Government Identity Proof").mandatory(true).build(),
                RequiredDocument.builder().scheme(s1).documentName("Class 12th Marksheet").description("Minimum 60% score required").mandatory(true).build(),
                RequiredDocument.builder().scheme(s1).documentName("Bank Passbook Copy").description("Aadhaar seeded bank account").mandatory(true).build()
        ));

        procedureRepository.saveAll(Arrays.asList(
                ApplicationProcedure.builder().scheme(s1).stepNumber(1).instruction("Visit National Scholarship Portal (NSP) and register as a new applicant.").build(),
                ApplicationProcedure.builder().scheme(s1).stepNumber(2).instruction("Fill student profile details and select 'National Higher Education Merit Scholarship'.").build(),
                ApplicationProcedure.builder().scheme(s1).stepNumber(3).instruction("Upload scanned income certificate and academic transcripts.").build(),
                ApplicationProcedure.builder().scheme(s1).stepNumber(4).instruction("Submit application for institute verification and track status online.").build()
        ));


        // --- SCHEME 2: PM-KISAN Samman Nidhi Scheme ---
        Scheme s2 = schemeRepository.save(Scheme.builder()
                .category(catAgri)
                .schemeName("PM Kisan Samman Nidhi")
                .shortDescription("Income support of ₹6,000 per year in 3 equal installments for farmer families.")
                .description("PM-KISAN is a Central Sector scheme with 100% funding from Government of India. Under the scheme, income support of ₹6,000/- per year in three equal installments is provided to all landholding farmer families.")
                .department("Ministry of Agriculture & Farmers Welfare")
                .schemeType("CENTRAL")
                .benefits("₹6,000 per year transferred directly into the bank accounts of beneficiaries in 4-monthly installments of ₹2,000.")
                .applicationUrl("https://pmkisan.gov.in")
                .contactInformation("PM-KISAN Helpline: 155261 / 011-24300606")
                .status(Status.ACTIVE)
                .build());

        ruleRepository.save(EligibilityRule.builder()
                .scheme(s2)
                .minAge(18)
                .maxAge(70)
                .gender("ALL")
                .state("ALL")
                .occupation("Farmer")
                .farmerStatus(true)
                .build());

        documentRepository.saveAll(Arrays.asList(
                RequiredDocument.builder().scheme(s2).documentName("Landholding Records (Khasra/Khatauni)").description("Valid land ownership document").mandatory(true).build(),
                RequiredDocument.builder().scheme(s2).documentName("Aadhaar Card").description("Identity & eKYC verification").mandatory(true).build(),
                RequiredDocument.builder().scheme(s2).documentName("Active Bank Account Details").description("Bank Passbook / IFSC code").mandatory(true).build()
        ));

        procedureRepository.saveAll(Arrays.asList(
                ApplicationProcedure.builder().scheme(s2).stepNumber(1).instruction("Open PM Kisan portal and navigate to 'Farmers Corner'.").build(),
                ApplicationProcedure.builder().scheme(s2).stepNumber(2).instruction("Click on 'New Farmer Registration' and enter Aadhaar Number.").build(),
                ApplicationProcedure.builder().scheme(s2).stepNumber(3).instruction("Enter land ownership details and upload property documents.").build(),
                ApplicationProcedure.builder().scheme(s2).stepNumber(4).instruction("Complete eKYC verification via OTP or biometric CSC.").build()
        ));


        // --- SCHEME 3: PMEGP Young Entrepreneurship Loan Grant ---
        Scheme s3 = schemeRepository.save(Scheme.builder()
                .category(catEnt)
                .schemeName("PMEGP Entrepreneurship Subsidized Loan Grant")
                .shortDescription("Subsidized credit loan up to ₹50 Lakhs with up to 35% government margin subsidy for new ventures.")
                .description("Prime Minister’s Employment Generation Programme (PMEGP) is a major credit-linked subsidy scheme aimed at generating self-employment opportunities through establishment of micro-enterprises in non-farm sector.")
                .department("Ministry of Micro, Small & Medium Enterprises (MSME)")
                .schemeType("CENTRAL")
                .benefits("Loan up to ₹50 Lakhs for manufacturing units and ₹20 Lakhs for service units with 15% to 35% margin subsidy.")
                .applicationUrl("https://www.kviconline.gov.in/pmegpeportal")
                .contactInformation("MSME Helpline: 1800-180-6763 | Email: pmegp.kvic@gov.in")
                .status(Status.ACTIVE)
                .build());

        ruleRepository.save(EligibilityRule.builder()
                .scheme(s3)
                .minAge(18)
                .maxAge(55)
                .maxIncome(500000.0)
                .gender("ALL")
                .state("ALL")
                .entrepreneurStatus(true)
                .build());

        documentRepository.saveAll(Arrays.asList(
                RequiredDocument.builder().scheme(s3).documentName("Project Report / Business Plan").description("Detailed DPR of proposed enterprise").mandatory(true).build(),
                RequiredDocument.builder().scheme(s3).documentName("Aadhaar & PAN Card").description("Identity & Tax verification").mandatory(true).build(),
                RequiredDocument.builder().scheme(s3).documentName("Educational Qualification Certificate").description("Class 8th / 10th / Degree pass certificate").mandatory(true).build()
        ));

        procedureRepository.saveAll(Arrays.asList(
                ApplicationProcedure.builder().scheme(s3).stepNumber(1).instruction("Log in to KVIC online PMEGP portal.").build(),
                ApplicationProcedure.builder().scheme(s3).stepNumber(2).instruction("Fill out online application form with applicant personal details and unit location.").build(),
                ApplicationProcedure.builder().scheme(s3).stepNumber(3).instruction("Upload Detailed Project Report (DPR) and identity documents.").build(),
                ApplicationProcedure.builder().scheme(s3).stepNumber(4).instruction("Application forwarded to designated Bank for loan appraisal and margin subsidy release.").build()
        ));


        // --- SCHEME 4: Tamil Nadu Chief Minister Youth Skill Development Scheme ---
        Scheme s4 = schemeRepository.save(Scheme.builder()
                .category(catEdu)
                .schemeName("Tamil Nadu Youth Skill Enhancement & Internship Scheme")
                .shortDescription("State stipend of ₹5,000/month for youth undergoing technical skill training in Tamil Nadu.")
                .description("Special State Government initiative for residents of Tamil Nadu to undergo industry-aligned technical skill development and apprenticeship programs.")
                .department("Tamil Nadu Skill Development Corporation (TNSDC)")
                .schemeType("STATE")
                .benefits("Free training + ₹5,000 monthly allowance for 6 months + Direct Industry Job Placement Assistance.")
                .applicationUrl("https://www.tnskill.tn.gov.in")
                .contactInformation("TNSDC Support: 044-22501011 | Email: info@tnskill.tn.gov.in")
                .status(Status.ACTIVE)
                .build());

        ruleRepository.save(EligibilityRule.builder()
                .scheme(s4)
                .minAge(18)
                .maxAge(30)
                .maxIncome(300000.0)
                .state("Tamil Nadu")
                .category("ALL")
                .studentStatus(true)
                .build());

        documentRepository.saveAll(Arrays.asList(
                RequiredDocument.builder().scheme(s4).documentName("Nativity / Domicile Certificate").description("Proof of Tamil Nadu residence").mandatory(true).build(),
                RequiredDocument.builder().scheme(s4).documentName("Degree / Diploma Pass Certificate").description("Educational proof").mandatory(true).build(),
                RequiredDocument.builder().scheme(s4).documentName("Community Certificate").description("Sub-category identification").mandatory(false).build()
        ));

        procedureRepository.saveAll(Arrays.asList(
                ApplicationProcedure.builder().scheme(s4).stepNumber(1).instruction("Register on TNSDC portal using Tamil Nadu domicile details.").build(),
                ApplicationProcedure.builder().scheme(s4).stepNumber(2).instruction("Select preferred skill course center in your district.").build(),
                ApplicationProcedure.builder().scheme(s4).stepNumber(3).instruction("Attend counseling and document verification.").build()
        ));


        // --- SCHEME 5: Ayushman Bharat Health Protection Insurance ---
        Scheme s5 = schemeRepository.save(Scheme.builder()
                .category(catHealth)
                .schemeName("Ayushman Bharat - PM-JAY Health Coverage")
                .shortDescription("Health cover of ₹5 Lakh per family per year for secondary and tertiary care hospitalization.")
                .description("World's largest health insurance scheme fully financed by government, providing coverage up to ₹5,00,000 per family per year for cash-less inpatient hospital treatment.")
                .department("National Health Authority (NHA)")
                .schemeType("CENTRAL")
                .benefits("Cashless healthcare up to ₹5,00,000 per family per year at empaneled public & private hospitals.")
                .applicationUrl("https://pmjay.gov.in")
                .contactInformation("PM-JAY Toll-Free Helpline: 14555 / 1800-111-565")
                .status(Status.ACTIVE)
                .build());

        ruleRepository.save(EligibilityRule.builder()
                .scheme(s5)
                .minAge(0)
                .maxAge(100)
                .maxIncome(200000.0)
                .gender("ALL")
                .state("ALL")
                .build());

        documentRepository.saveAll(Arrays.asList(
                RequiredDocument.builder().scheme(s5).documentName("Ration Card / SECC 2011 Record").description("Proof of family inclusion").mandatory(true).build(),
                RequiredDocument.builder().scheme(s5).documentName("Aadhaar Card").description("Biometric identification").mandatory(true).build()
        ));

        procedureRepository.saveAll(Arrays.asList(
                ApplicationProcedure.builder().scheme(s5).stepNumber(1).instruction("Visit Amrit / PMJAY counter at any empaneled hospital or CSC center.").build(),
                ApplicationProcedure.builder().scheme(s5).stepNumber(2).instruction("Verify your family status using Ration Card or Aadhaar.").build(),
                ApplicationProcedure.builder().scheme(s5).stepNumber(3).instruction("Receive Ayushman Card for instant cashless medical treatments.").build()
        ));

        System.out.println(">>> Seeded Categories and 5 Real Government Schemes with Predefined Rules!");
    }
}
