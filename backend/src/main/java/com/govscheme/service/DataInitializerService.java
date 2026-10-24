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

        seedCategoriesAndSchemes();
    }

    private void seedUsers() {
        // 1. Admin User
        User admin = User.builder()
                .name("System Administrator")
                .email("admin@gov.in")
                .mobile("9876543210")
                .aadhaarNumber("112233445566")
                .aadhaarVerified(true)
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
                .aadhaarNumber("998877665544")
                .aadhaarVerified(true)
                .password(passwordEncoder.encode("user123"))
                .role(Role.ROLE_USER)
                .status(Status.ACTIVE)
                .build();
        citizen = userRepository.save(citizen);

        UserProfile citizenProfile = UserProfile.builder()
                .user(citizen)
                .aadhaarNumber("998877665544")
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

    private SchemeCategory getOrCreateCategory(String name, String description, String iconName) {
        return categoryRepository.findByName(name).orElseGet(() -> categoryRepository.save(
                SchemeCategory.builder()
                        .name(name)
                        .description(description)
                        .iconName(iconName)
                        .status(Status.ACTIVE)
                        .build()
        ));
    }

    private void createSchemeIfMissing(
            SchemeCategory category,
            String schemeName,
            String shortDescription,
            String description,
            String department,
            String schemeType,
            String benefits,
            String applicationUrl,
            String contactInfo,
            EligibilityRule rule,
            List<RequiredDocument> documents,
            List<ApplicationProcedure> procedures) {

        if (schemeRepository.existsBySchemeName(schemeName)) {
            return;
        }

        Scheme scheme = schemeRepository.save(Scheme.builder()
                .category(category)
                .schemeName(schemeName)
                .shortDescription(shortDescription)
                .description(description)
                .department(department)
                .schemeType(schemeType)
                .benefits(benefits)
                .applicationUrl(applicationUrl)
                .contactInformation(contactInfo)
                .status(Status.ACTIVE)
                .build());

        if (rule != null) {
            rule.setScheme(scheme);
            ruleRepository.save(rule);
        }

        if (documents != null && !documents.isEmpty()) {
            documents.forEach(doc -> doc.setScheme(scheme));
            documentRepository.saveAll(documents);
        }

        if (procedures != null && !procedures.isEmpty()) {
            procedures.forEach(p -> p.setScheme(scheme));
            procedureRepository.saveAll(procedures);
        }
    }

    private void seedCategoriesAndSchemes() {
        // Categories
        SchemeCategory catEdu = getOrCreateCategory(
                "Education & Student Welfare",
                "Scholarships, financial stipends, and educational grants for students.",
                "bi-mortarboard-fill"
        );

        SchemeCategory catAgri = getOrCreateCategory(
                "Agriculture & Farming",
                "Financial assistance, crop insurance, and equipment subsidies for farmers.",
                "bi-flower1"
        );

        SchemeCategory catStartup = getOrCreateCategory(
                "Startups & Entrepreneurship",
                "Seed funds, incubation grants, collateral-free credit, and startup capital support.",
                "bi-rocket-takeoff-fill"
        );

        SchemeCategory catSenior = getOrCreateCategory(
                "Senior Citizens & Pension Welfare",
                "Old age monthly pensions, guaranteed retirement income, and assistive geriatric healthcare.",
                "bi-person-heart"
        );

        SchemeCategory catHealth = getOrCreateCategory(
                "Healthcare & Wellness",
                "Universal health insurance, free hospitalization, and medical aid.",
                "bi-heart-pulse-fill"
        );

        SchemeCategory catWomen = getOrCreateCategory(
                "Women & Child Empowerment",
                "Maternity allowances, women-led enterprise grants, and self-help group funding.",
                "bi-gender-female"
        );

        SchemeCategory catSocial = getOrCreateCategory(
                "Social Welfare & Housing Assistance",
                "Housing subsidies, disability support grants, and community financial upliftment.",
                "bi-people-fill"
        );

        // =========================================================================
        // SECTION 1: SCHEMES FOR OLD AGED / SENIOR CITIZENS
        // =========================================================================

        // 1. Indira Gandhi National Old Age Pension Scheme (IGNOAPS)
        createSchemeIfMissing(
                catSenior,
                "Indira Gandhi National Old Age Pension Scheme (IGNOAPS)",
                "Monthly non-contributory cash pension for elderly citizens aged 60+ from economically weaker families.",
                "A flagship central social assistance programme providing monthly direct pension transfer to senior citizens who are 60 years of age or above belonging to Below Poverty Line (BPL) households.",
                "Ministry of Rural Development (MoRD)",
                "CENTRAL",
                "Monthly direct bank pension of ₹500 to ₹1,000 + Priority access to State Geriatric Healthcare facilities.",
                "https://nsap.nic.in",
                "NSAP Toll-Free Helpline: 1800-111-555 | Email: nsap-support@gov.in",
                EligibilityRule.builder()
                        .minAge(60)
                        .maxAge(120)
                        .maxIncome(150000.0)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Age & identity proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("BPL Ration Card / Income Certificate").description("Proof of family income below poverty line").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Bank / Post Office Passbook").description("Aadhaar-seeded bank account for pension DBT").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Visit nearest Gram Panchayat or Block Development Office or Municipal Welfare Desk.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Fill NSAP Application Form and attach Aadhaar and BPL card copies.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Verification of age and residential eligibility by Village Administrative Officer (VAO).").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Sanction order issued and monthly pension credited automatically to bank account.").build()
                )
        );

        // 2. Pradhan Mantri Vaya Vandana Yojana (PMVVY)
        createSchemeIfMissing(
                catSenior,
                "Pradhan Mantri Vaya Vandana Yojana (PMVVY)",
                "Government retirement pension plan providing assured 7.4% annual return payable monthly for 10 years.",
                "A subsidized pension scheme exclusively for senior citizens aged 60 years and above, operated through Life Insurance Corporation of India (LIC) to protect elderly individuals against market volatility.",
                "Ministry of Finance / LIC of India",
                "CENTRAL",
                "Assured pension return of 7.40% p.a. payable monthly (up to ₹9,250/month) + 100% purchase price refund on maturity/nominee payout.",
                "https://licindia.in",
                "LIC Pension Helpdesk: 022-68276827 | Email: co_pension@licindia.com",
                EligibilityRule.builder()
                        .minAge(60)
                        .maxAge(120)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Proof of age (60+) and identity").mandatory(true).build(),
                        RequiredDocument.builder().documentName("PAN Card").description("Tax identification for pension deposit").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Bank Account Details & Cancelled Cheque").description("Direct ECS pension deposit").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Log in to LIC portal or visit any authorized LIC branch.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Select PMVVY plan and choose pension payment frequency (Monthly/Quarterly/Annual).").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Deposit purchase price corpus (up to ₹15 Lakhs).").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Receive immediate pension policy document and automatic monthly credits.").build()
                )
        );

        // 3. Rashtriya Vayoshri Yojana (RVY) - Assistive Living Devices for Seniors
        createSchemeIfMissing(
                catSenior,
                "Rashtriya Vayoshri Yojana (RVY)",
                "Free distribution of assisted-living physical aids and devices for senior citizens with age-related disabilities.",
                "A central sector scheme providing physical aids and assisted-living devices for senior citizens belonging to BPL category suffering from any age-related disability or infirmity such as low vision, hearing impairment, or locomotor disability.",
                "Ministry of Social Justice & Empowerment",
                "CENTRAL",
                "100% free distribution of customized wheelchairs, hearing aids, spectacles, walking sticks, walkers, and artificial dentures.",
                "https://socialjustice.gov.in",
                "Department of Social Justice Helpline: 1800-11-2001",
                EligibilityRule.builder()
                        .minAge(60)
                        .maxAge(120)
                        .maxIncome(200000.0)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Age & identity authentication").mandatory(true).build(),
                        RequiredDocument.builder().documentName("BPL Certificate or Annual Income Proof").description("Income below ₹2,00,000 p.a.").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Medical Certificate / Disability Slip").description("Certified by government medical officer").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Register at the designated ALIMCO camp or District Social Welfare Office.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Undergo free clinical and assistive assessment by medical specialists.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Receive personalized assisted-living equipment on the distribution day free of cost.").build()
                )
        );

        // 4. Ayushman Bharat PM-JAY Senior Citizen 70+ Universal Health Cover
        createSchemeIfMissing(
                catSenior,
                "Ayushman Bharat Senior Citizen 70+ Universal Health Cover",
                "Universal free healthcare coverage of ₹5 Lakhs per family per year for all senior citizens aged 70 and above, regardless of income.",
                "Historic Union Cabinet approved expansion providing universal health insurance cover of ₹5 Lakh per year for all senior citizens aged 70 years and above across India, irrespective of their socio-economic status.",
                "National Health Authority (NHA)",
                "CENTRAL",
                "Free cashless hospital treatments up to ₹5,00,000 annually across 29,000+ empaneled government and private hospitals nationwide.",
                "https://beneficiary.nha.gov.in",
                "National Health Authority Toll-Free: 14555",
                EligibilityRule.builder()
                        .minAge(70)
                        .maxAge(120)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Mandatory for age (70+) verification via e-KYC").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Visit beneficiary.nha.gov.in portal or download the Ayushman App.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Authenticate your identity using 12-digit Aadhaar e-KYC OTP.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Download your dedicated Ayushman Vay Vandana Card with unique 14-digit ABHA ID.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Show card at any empaneled hospital for instant cashless admissions and surgeries.").build()
                )
        );

        // 5. Atal Pension Yojana (APY)
        createSchemeIfMissing(
                catSenior,
                "Atal Pension Yojana (APY)",
                "Guaranteed monthly pension of ₹1,000 to ₹5,000 starting at age 60 for unorganized workers and self-employed citizens.",
                "A government-backed social security pension scheme focused on the unorganized sector, ensuring a fixed monthly lifetime pension starting at age 60, followed by pension to spouse and full corpus payout to nominees.",
                "Pension Fund Regulatory and Development Authority (PFRDA)",
                "CENTRAL",
                "Guaranteed monthly lifetime pension of ₹1,000, ₹2,000, ₹3,000, ₹4,000 or ₹5,000 from age 60.",
                "https://www.npscra.nsdl.co.in",
                "APY Call Centre: 1800-110-069",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(40)
                        .maxIncome(400000.0)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Aadhaar Card").description("UIDAI verification").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Bank / Post Office Account").description("Auto-debit mandate setup").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Nominee Details").description("Nominee relationship and Aadhaar").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Approach the bank where you hold an active savings bank account.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Fill out APY Subscriber Registration Form and choose desired pension slab.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Set up auto-debit of modest monthly contribution.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Receive Permanent Retirement Account Number (PRAN) confirmation.").build()
                )
        );

        // =========================================================================
        // SECTION 2: SCHEMES FOR STARTUPS & ENTREPRENEURS
        // =========================================================================

        // 6. Startup India Seed Fund Scheme (SISFS)
        createSchemeIfMissing(
                catStartup,
                "Startup India Seed Fund Scheme (SISFS)",
                "Early-stage capital grants up to ₹20 Lakhs for prototyping & up to ₹50 Lakhs for market commercialization.",
                "A premier government seed fund providing financial assistance to DPIIT-recognized early-stage startups for proof of concept, prototype development, product trials, market entry, and commercialization through approved incubators across India.",
                "Department for Promotion of Industry and Internal Trade (DPIIT)",
                "CENTRAL",
                "Grant up to ₹20 Lakhs for Proof of Concept / prototype validation + Debt / Convertible debentures up to ₹50 Lakhs for commercial launch.",
                "https://seedfund.startupindia.gov.in",
                "Startup India Hub Helpline: 1800-115-565 | Email: dipp-startups@nic.in",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(65)
                        .entrepreneurStatus(true)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("DPIIT Startup Recognition Certificate").description("Valid startup recognition certificate").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Pitch Deck & Business Plan").description("Detailed DPR with technology milestone roadmap").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Company Incorporation Certificate / GST").description("MCA registration certificate").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Founders' Aadhaar & PAN Cards").description("Identity proof of founding directors").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Log in to seedfund.startupindia.gov.in using Startup India credentials.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Select up to 3 preferred incubators aligned with your industry sector.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Submit project presentation and fund utilization schedule.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Pitch to the Incubator Seed Management Committee (ISMC) for milestone disbursements.").build()
                )
        );

        // 7. Pradhan Mantri Mudra Yojana (PMMY)
        createSchemeIfMissing(
                catStartup,
                "Pradhan Mantri Mudra Yojana (PMMY)",
                "Collateral-free business loans up to ₹20 Lakhs for micro-enterprises, small businesses, and emerging startups.",
                "Flagship micro-credit scheme providing collateral-free institutional financing up to ₹20 Lakhs across Shishu, Kishore, Tarun, and Tarun Plus categories for non-corporate, non-farm small/micro enterprises.",
                "Ministry of Finance / MUDRA",
                "CENTRAL",
                "Collateral-free loans up to ₹20 Lakhs at competitive interest rates + Mudra RuPay card for flexible working capital withdrawals.",
                "https://www.mudra.org.in",
                "MUDRA National Toll-Free: 1800-180-1111",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(65)
                        .entrepreneurStatus(true)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Aadhaar & PAN Card").description("Identity and KYC proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Business Registration / Udyam Certificate").description("Proof of business entity").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Quotation of Machinery / Project Report").description("Cost estimation for requested loan amount").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Bank Account Statement (Past 6 months)").description("Financial banking history").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Visit udyamimitra.in or approach any commercial bank / NBFC.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Select appropriate Mudra category (Shishu, Kishore, or Tarun).").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Submit application along with equipment quotations and Udyam certificate.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Bank approves loan without collateral and issues Mudra debit card.").build()
                )
        );

        // 8. Stand-Up India Scheme for Women & SC/ST Entrepreneurs
        createSchemeIfMissing(
                catStartup,
                "Stand-Up India Scheme for Women and SC/ST Entrepreneurs",
                "Bank loans from ₹10 Lakhs to ₹1 Crore for setting up greenfield enterprises in manufacturing, services, or agri-allied sectors.",
                "A national initiative facilitating bank loans between ₹10 Lakhs and ₹1 Crore to at least one Scheduled Caste (SC) or Scheduled Tribe (ST) borrower and at least one woman entrepreneur per bank branch for setting up greenfield enterprises.",
                "Ministry of Finance / SIDBI",
                "CENTRAL",
                "Composite loan covering 75% to 85% of project cost (up to ₹1 Crore) + Credit Guarantee Fund support without third-party collateral.",
                "https://www.standupmitra.in",
                "Stand-Up Mitra Helpline: 1800-180-1159",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(65)
                        .entrepreneurStatus(true)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Proof of Category (SC/ST Certificate or Female Identity)").description("Mandatory qualification document").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Detailed Greenfield Project Report (DPR)").description("Business viability study and financial projections").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar & PAN Card").description("Identity & tax verification").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Pollution / Municipal NOC (if applicable)").description("Regulatory approvals for proposed unit").mandatory(false).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Register on the Stand-Up Mitra portal (standupmitra.in).").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Opt for handholding support (trainee borrower) or apply directly to preferred bank branch.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Bank appraises project report and secures credit guarantee under CGSUS.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Sanction of term loan and working capital limits with margin money subsidy.").build()
                )
        );

        // 9. Credit Guarantee Scheme for Startups (CGSS)
        createSchemeIfMissing(
                catStartup,
                "Credit Guarantee Scheme for Startups (CGSS)",
                "Collateral-free debt loan guarantee up to ₹10 Crores per DPIIT-recognized startup through financial institutions.",
                "A landmark financing scheme that enables commercial scheduled banks, NBFCs, and AIFs to extend collateral-free term loans and venture debt up to ₹10 Crores to DPIIT-recognized startups with up to 85% government guarantee coverage.",
                "Department for Promotion of Industry and Internal Trade (DPIIT) / NCGTC",
                "CENTRAL",
                "Government credit guarantee cover up to 85% for debt facilities up to ₹5 Crores and 75% for loans between ₹5-10 Crores.",
                "https://www.ncgtc.in",
                "NCGTC Support Desk: 022-61501100",
                EligibilityRule.builder()
                        .minAge(21)
                        .maxAge(60)
                        .entrepreneurStatus(true)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("DPIIT Certificate of Startup Recognition").description("Active startup status").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Audited Financials / Revenue Projections").description("Certified by Chartered Accountant").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Board Resolution & Founder Guarantees").description("Authorized corporate governance filings").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Approach any Member Lending Institution (Scheduled Commercial Bank or Venture Debt Fund).").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Present credit proposal with business milestones and DPIIT credentials.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Lending bank submits guarantee application to NCGTC.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Collateral-free credit facility disbursed to startup corporate bank account.").build()
                )
        );

        // 10. Atal Innovation Mission (AIM) - AIC Startup Incubation Support
        createSchemeIfMissing(
                catStartup,
                "Atal Innovation Mission - Incubation & Technology Seed Support",
                "World-class incubation, rapid prototyping lab access, and seed grants up to ₹10 Crores for innovative tech ventures.",
                "NITI Aayog's flagship innovation initiative establishing world-class Atal Incubation Centres (AICs) that provide state-of-the-art infrastructure, high-end fabrication equipment, mentor networks, and seed funding for deep-tech, healthcare, and agritech startups.",
                "NITI Aayog / Atal Innovation Mission",
                "CENTRAL",
                "Free access to rapid prototyping labs, co-working spaces, legal/patent advisory + Up to ₹25 Lakhs proof-of-concept grant.",
                "https://aim.gov.in",
                "AIM Helpdesk: 011-23096620 | Email: md-aim@gov.in",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(55)
                        .entrepreneurStatus(true)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Technology Pitch Deck & Patent / IP Details").description("Novelty description and prototype demonstration").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Founders' Resume & Aadhaar Details").description("Technical background of founders").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Company Incorporation Certificate").description("CIN / LLPIN registration").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Browse network of 70+ Atal Incubation Centres across universities and research parks.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Apply for incubation cohort through online portal with technical whitepaper.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Participate in pitch day evaluation before technical selection jury.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Sign incubation agreement and access laboratories, computing clusters, and angel seed funds.").build()
                )
        );

        // 11. Prime Minister's Employment Generation Programme (PMEGP)
        createSchemeIfMissing(
                catStartup,
                "PMEGP Entrepreneurship Subsidized Loan Grant",
                "Subsidized credit loan up to ₹50 Lakhs with up to 35% government margin subsidy for new ventures.",
                "Prime Minister’s Employment Generation Programme (PMEGP) is a major credit-linked subsidy scheme aimed at generating self-employment opportunities through establishment of micro-enterprises in non-farm sector.",
                "Ministry of Micro, Small & Medium Enterprises (MSME)",
                "CENTRAL",
                "Loan up to ₹50 Lakhs for manufacturing units and ₹20 Lakhs for service units with 15% to 35% margin subsidy.",
                "https://www.kviconline.gov.in/pmegpeportal",
                "MSME Helpline: 1800-180-6763 | Email: pmegp.kvic@gov.in",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(55)
                        .maxIncome(500000.0)
                        .entrepreneurStatus(true)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Project Report / Business Plan").description("Detailed DPR of proposed enterprise").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar & PAN Card").description("Identity & Tax verification").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Educational Qualification Certificate").description("Class 8th / 10th / Degree pass certificate").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Log in to KVIC online PMEGP portal.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Fill out online application form with applicant personal details and unit location.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Upload Detailed Project Report (DPR) and identity documents.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Application forwarded to designated Bank for loan appraisal and margin subsidy release.").build()
                )
        );

        // =========================================================================
        // SECTION 3: SCHEMES FOR WOMEN & CHILD EMPOWERMENT
        // =========================================================================

        // 12. Pradhan Mantri Matru Vandana Yojana (PMMVY)
        createSchemeIfMissing(
                catWomen,
                "Pradhan Mantri Matru Vandana Yojana (PMMVY)",
                "Direct cash maternity benefit of ₹5,000 to ₹6,000 in bank account for pregnant women and lactating mothers.",
                "A maternity benefit programme providing cash incentives for pregnant women and lactating mothers to partially compensate for wage loss and promote institutional deliveries and infant vaccination.",
                "Ministry of Women and Child Development",
                "CENTRAL",
                "Cash transfer of ₹5,000 for the first child and ₹6,000 for the second girl child directly credited via DBT.",
                "https://pmmvy.wcd.gov.in",
                "PMMVY National Helpline: 011-23382393",
                EligibilityRule.builder()
                        .minAge(19)
                        .maxAge(45)
                        .maxIncome(800000.0)
                        .gender("Female")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Mother and Child Protection (MCP) Card").description("Issued by Primary Health Centre").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card of Mother and Husband").description("Identity proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Bank Passbook Copy").description("Aadhaar-seeded bank account").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Register at the local Anganwadi Centre or on pmmvy.wcd.gov.in within 570 days of LMP.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Undergo required antenatal health checkups (ANC) and record in MCP card.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("First installment credited upon ANC checkup; second installment upon child birth and vaccinations.").build()
                )
        );

        // 13. Lakhpati Didi Rural Women Entrepreneurship Scheme
        createSchemeIfMissing(
                catWomen,
                "Lakhpati Didi Rural Women Entrepreneurship Scheme",
                "Vocational training, drone pilot certification, and zero-interest micro-credit for women in Self-Help Groups (SHGs).",
                "A national mission enabling rural women members of Deendayal Antyodaya Yojana - National Rural Livelihoods Mission (DAY-NRLM) Self Help Groups (SHGs) to earn a sustainable annual household income of at least ₹1 Lakh through diverse micro-enterprises.",
                "Ministry of Rural Development / DAY-NRLM",
                "CENTRAL",
                "Free skill training (Drone Didi, LED fabrication, organic farming) + Revolving Community Investment Fund loans up to ₹5 Lakhs.",
                "https://nrlm.gov.in",
                "DAY-NRLM Helpline: 1800-111-222",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(60)
                        .maxIncome(300000.0)
                        .gender("Female")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("SHG Membership Passbook / Certificate").description("Proof of active Self-Help Group membership").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Identity proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Bank Account Details").description("Individual / SHG joint account").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Enroll through your Gram Panchayat Village Organization (VO) or Cluster Level Federation (CLF).").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Choose enterprise trade (Drone Pilot, Food Processing, Handloom, Solar Technician).").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Complete practical training and receive national certification.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Avail revolving community fund micro-credit to procure equipment and start business.").build()
                )
        );

        // =========================================================================
        // SECTION 4: SOCIAL WELFARE, HOUSING & DISABILITY SCHEMES
        // =========================================================================

        // 14. Pradhan Mantri Awas Yojana (PMAY - Housing for All)
        createSchemeIfMissing(
                catSocial,
                "Pradhan Mantri Awas Yojana (PMAY - Housing for All)",
                "Up to ₹2.67 Lakhs upfront credit-linked interest subsidy and construction assistance for pucca housing.",
                "Flagship housing mission aimed at providing pucca houses with basic amenities to all eligible urban and rural families belonging to EWS, LIG, and Middle Income categories.",
                "Ministry of Housing & Urban Affairs (MoHUA)",
                "CENTRAL",
                "Direct credit subsidy up to ₹2.67 Lakhs on home loans + Direct grant of ₹1.20 Lakhs to ₹1.30 Lakhs for rural home construction.",
                "https://pmaymis.gov.in",
                "PMAY Urban Toll-Free: 1800-11-3377 / 1800-11-6163",
                EligibilityRule.builder()
                        .minAge(21)
                        .maxAge(70)
                        .maxIncome(600000.0)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Aadhaar Cards of all Family Members").description("Mandatory for family de-duplication").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Income Certificate / Form 16 / ITR").description("Proof of EWS / LIG category income").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Land Title / Agreement to Sale / Allotment Letter").description("Property documentation").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Affidavit of Non-Ownership of Pucca House").description("Self-declaration in standard format").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Apply online on pmaymis.gov.in or through nearest Citizen Service Centre (CSC).").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Submit application under 'Benefit under other 3 components' or 'Credit Linked Subsidy (CLSS)'.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Primary Lending Institution (Bank) evaluates eligibility and forwards claim to Central Nodal Agency.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Interest subsidy is credited directly to beneficiary loan account, reducing EMI.").build()
                )
        );

        // 15. Divyangjan Swavalamban - ADIP Scheme for Persons with Disabilities
        createSchemeIfMissing(
                catSocial,
                "Divyangjan Swavalamban - ADIP Scheme for Persons with Disabilities",
                "100% financial grant for high-end prosthetics, motorized tricycles, braille devices, and 4% concessional business loans.",
                "Assistance to Disabled Persons for Purchase/Fitting of Aids and Appliances (ADIP) scheme providing durable, modern, scientifically manufactured aids and concessional self-employment credit to empower persons with disabilities (PwDs).",
                "Department of Empowerment of Persons with Disabilities (DEPwD)",
                "CENTRAL",
                "Free motorized tricycles, smart canes, digital hearing aids + Concessional business loan up to ₹25 Lakhs at just 4% interest rate.",
                "https://swavlambancard.gov.in",
                "ALIMCO Toll-Free Helpline: 1800-180-5129",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(65)
                        .maxIncome(300000.0)
                        .disabilityStatus(true)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("UDID Card / Disability Certificate").description("Minimum 40% certified disability").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Identity proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Income Certificate").description("Annual family income proof").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Register with your 18-digit Unique Disability ID (UDID) on swavlambancard.gov.in.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Select required assistive device or enterprise loan module.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Attend assessment camp conducted by ALIMCO / District Rehabilitation Centre.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Receive high-end assistive appliance fitted by rehabilitation experts free of charge.").build()
                )
        );

        // =========================================================================
        // SECTION 5: EDUCATION & AGRICULTURE (EXISTING + ENHANCED)
        // =========================================================================

        // 16. National Higher Education Merit Scholarship
        createSchemeIfMissing(
                catEdu,
                "National Higher Education Merit Scholarship",
                "Financial stipend of ₹20,000/year for underprivileged students pursuing higher education.",
                "A comprehensive merit-cum-means scholarship scheme designed to encourage talented students from low-income families to pursue undergraduate and postgraduate education without financial hardship.",
                "Department of Higher Education",
                "CENTRAL",
                "₹20,000 annual direct cash transfer to bank account + Tuition Fee Waiver up to ₹50,000.",
                "https://scholarships.gov.in",
                "Toll Free Helpline: 1800-111-222 | Email: support-nsp@gov.in",
                EligibilityRule.builder()
                        .minAge(17)
                        .maxAge(25)
                        .maxIncome(250000.0)
                        .gender("ALL")
                        .state("ALL")
                        .occupation("Student")
                        .studentStatus(true)
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Income Certificate").description("Issued by Tehsildar (valid for current FY)").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Government Identity Proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Class 12th Marksheet").description("Minimum 60% score required").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Bank Passbook Copy").description("Aadhaar seeded bank account").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Visit National Scholarship Portal (NSP) and register as a new applicant.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Fill student profile details and select 'National Higher Education Merit Scholarship'.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Upload scanned income certificate and academic transcripts.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Submit application for institute verification and track status online.").build()
                )
        );

        // 17. Post-Matric Scholarship for SC/ST/OBC Students
        createSchemeIfMissing(
                catEdu,
                "Central Post-Matric Scholarship for Underprivileged Students",
                "100% compulsory non-refundable fees reimbursement + monthly maintenance allowance up to ₹13,500/year for professional degrees.",
                "Centrally sponsored scheme providing comprehensive financial assistance to SC, ST, and OBC students studying at post-matriculation or post-secondary stage to enable them to complete higher education in medicine, engineering, law, and arts.",
                "Ministry of Social Justice and Empowerment",
                "CENTRAL",
                "Complete tuition fee reimbursement directly to educational institution + living maintenance stipend up to ₹13,500 annually.",
                "https://scholarships.gov.in",
                "NSP Helpdesk: 0120-6619540",
                EligibilityRule.builder()
                        .minAge(16)
                        .maxAge(35)
                        .maxIncome(250000.0)
                        .studentStatus(true)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Caste / Community Certificate").description("Certified by revenue authority").mandatory(true).build(),
                        RequiredDocument.builder().documentName("College Admission & Fee Structure Slip").description("Institutional bonafide certificate").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Income Certificate").description("Family income below ₹2.5 Lakhs").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Biometric identity proof").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Apply online on the National Scholarship Portal under State/Central Post-Matric section.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Enter college roll number and upload fee receipt and community certificate.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("College verification officer verifies bonafide student status.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("District Welfare Officer releases fee reimbursement directly via PFMS DBT.").build()
                )
        );

        // 18. PM Kisan Samman Nidhi
        createSchemeIfMissing(
                catAgri,
                "PM Kisan Samman Nidhi",
                "Income support of ₹6,000 per year in 3 equal installments for farmer families.",
                "PM-KISAN is a Central Sector scheme with 100% funding from Government of India. Under the scheme, income support of ₹6,000/- per year in three equal installments is provided to all landholding farmer families.",
                "Ministry of Agriculture & Farmers Welfare",
                "CENTRAL",
                "₹6,000 per year transferred directly into the bank accounts of beneficiaries in 4-monthly installments of ₹2,000.",
                "https://pmkisan.gov.in",
                "PM-KISAN Helpline: 155261 / 011-24300606",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(75)
                        .gender("ALL")
                        .state("ALL")
                        .occupation("Farmer")
                        .farmerStatus(true)
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Landholding Records (Khasra/Khatauni)").description("Valid land ownership document").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Identity & eKYC verification").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Active Bank Account Details").description("Bank Passbook / IFSC code").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Open PM Kisan portal and navigate to 'Farmers Corner'.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Click on 'New Farmer Registration' and enter Aadhaar Number.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Enter land ownership details and upload property documents.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Complete eKYC verification via OTP or biometric CSC.").build()
                )
        );

        // 19. Tamil Nadu Youth Skill Enhancement & Internship Scheme
        createSchemeIfMissing(
                catEdu,
                "Tamil Nadu Youth Skill Enhancement & Internship Scheme",
                "State stipend of ₹5,000/month for youth undergoing technical skill training in Tamil Nadu.",
                "Special State Government initiative for residents of Tamil Nadu to undergo industry-aligned technical skill development and apprenticeship programs.",
                "Tamil Nadu Skill Development Corporation (TNSDC)",
                "STATE",
                "Free training + ₹5,000 monthly allowance for 6 months + Direct Industry Job Placement Assistance.",
                "https://www.tnskill.tn.gov.in",
                "TNSDC Support: 044-22501011 | Email: info@tnskill.tn.gov.in",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(30)
                        .maxIncome(300000.0)
                        .state("Tamil Nadu")
                        .category("ALL")
                        .studentStatus(true)
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Nativity / Domicile Certificate").description("Proof of Tamil Nadu residence").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Degree / Diploma Pass Certificate").description("Educational proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Community Certificate").description("Sub-category identification").mandatory(false).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Register on TNSDC portal using Tamil Nadu domicile details.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Select preferred skill course center in your district.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Attend counseling and document verification.").build()
                )
        );

        // 20. Ayushman Bharat - PM-JAY Health Coverage
        createSchemeIfMissing(
                catHealth,
                "Ayushman Bharat - PM-JAY Health Coverage",
                "Health cover of ₹5 Lakh per family per year for secondary and tertiary care hospitalization.",
                "World's largest health insurance scheme fully financed by government, providing coverage up to ₹5,00,000 per family per year for cash-less inpatient hospital treatment.",
                "National Health Authority (NHA)",
                "CENTRAL",
                "Cashless healthcare up to ₹5,00,000 per family per year at empaneled public & private hospitals.",
                "https://pmjay.gov.in",
                "PM-JAY Toll-Free Helpline: 14555 / 1800-111-565",
                EligibilityRule.builder()
                        .minAge(0)
                        .maxAge(100)
                        .maxIncome(250000.0)
                        .gender("ALL")
                        .state("ALL")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Ration Card / SECC 2011 Record").description("Proof of family inclusion").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Biometric identification").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Visit Amrit / PMJAY counter at any empaneled hospital or CSC center.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Verify your family status using Ration Card or Aadhaar.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Receive Ayushman Card for instant cashless medical treatments.").build()
                )
        );

        // =========================================================================
        // SECTION 8: STATE-SPECIFIC GOVERNMENT WELFARE SCHEMES
        // =========================================================================

        // --- TAMIL NADU STATE SCHEMES ---

        // 21. Kalaignar Magalir Urimai Thittam (KMUT) - Tamil Nadu
        createSchemeIfMissing(
                catWomen,
                "Kalaignar Magalir Urimai Thittam (KMUT)",
                "Direct monthly cash grant of ₹1,000 for women family heads residing in Tamil Nadu.",
                "A landmark social welfare initiative by the Government of Tamil Nadu acknowledging the unpaid household care work of women by transferring ₹1,000 monthly directly to eligible women heads of families.",
                "Special Programme Implementation Department, Govt of Tamil Nadu",
                "STATE",
                "Direct monthly bank credit of ₹1,000 (₹12,000 per year) transferred to Aadhaar-seeded bank account.",
                "https://kmut.tn.gov.in",
                "KMUT Support Helpline: 044-25619200 | CM Helpline: 1100",
                EligibilityRule.builder()
                        .minAge(21)
                        .maxAge(65)
                        .gender("Female")
                        .maxIncome(250000.0)
                        .state("Tamil Nadu")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Smart Ration Card (Family Card)").description("Proof of family and head of household").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Identity and age proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Electricity Consumer Card / Bill").description("Residential verification (under 3,600 units/year)").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar-Linked Bank Passbook").description("Direct Benefit Transfer (DBT) account").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Obtain application token from your fair price shop (ration shop) during camp distribution.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Attend designated ward or village camp with smart ration card, Aadhaar, and electricity bill.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Complete biometric and OTP verification with the camp officer.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Upon verification by field revenue staff, monthly stipend of ₹1,000 is credited automatically on the 15th of each month.").build()
                )
        );

        // 22. Pudhumai Penn Scheme (Higher Education Assurance) - Tamil Nadu
        createSchemeIfMissing(
                catEdu,
                "Pudhumai Penn Scheme - Higher Education Assurance",
                "Monthly financial assistance of ₹1,000 for female students from govt schools pursuing higher education.",
                "Moovalur Ramamirtham Ammaiyar Higher Education Assurance Scheme provides ₹1,000 monthly stipend to female students who studied from Class 6 to 12 in Tamil Nadu Government schools, deposited throughout their undergraduate or diploma studies.",
                "Social Welfare & Women Empowerment Dept, Govt of Tamil Nadu",
                "STATE",
                "₹1,000 monthly scholarship deposited directly to student bank account till completion of degree or diploma.",
                "https://www.penkalvi.tn.gov.in",
                "Moovalur Ramamirtham Ammaiyar Scheme Desk: 044-28592750",
                EligibilityRule.builder()
                        .minAge(17)
                        .maxAge(26)
                        .gender("Female")
                        .studentStatus(true)
                        .maxIncome(300000.0)
                        .state("Tamil Nadu")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Classes 6-12 Govt School Certificate / EMIS ID").description("Proof of schooling in Tamil Nadu Government schools").mandatory(true).build(),
                        RequiredDocument.builder().documentName("College Admission Receipt / Student ID Card").description("Proof of active college or diploma enrollment").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Identity proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Bank Passbook in Student's Name").description("Aadhaar-linked account for DBT").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Apply through your college nodal officer on the Penkalvi (pudhumai penn) portal.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Provide EMIS school education number to verify continuous schooling in TN government schools.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("College Principal verifies and approves student eligibility.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Monthly ₹1,000 scholarship credited directly to student bank account every month.").build()
                )
        );

        // 23. Chief Minister Comprehensive Health Insurance Scheme (CMCHIS) - Tamil Nadu
        createSchemeIfMissing(
                catHealth,
                "Chief Minister Comprehensive Health Insurance Scheme (CMCHIS)",
                "Cashless hospitalization coverage up to ₹5,00,000 per family per year for Tamil Nadu residents.",
                "State flagship health cover offering comprehensive cashless medical, surgical, and diagnostic treatments across 1,090 procedures in public and empanelled private hospitals for low-income families in Tamil Nadu.",
                "Health and Family Welfare Department, Govt of Tamil Nadu",
                "STATE",
                "Up to ₹5 Lakh cashless hospital treatment per family per year + emergency transport and post-operative medications.",
                "https://www.cmchistn.com",
                "CMCHIS 24x7 Toll-Free Call Centre: 1800-425-3993",
                EligibilityRule.builder()
                        .minAge(0)
                        .maxAge(110)
                        .maxIncome(120000.0)
                        .state("Tamil Nadu")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Smart Ration Card").description("Family card showing Tamil Nadu address").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Income Certificate from VAO / Tahsildar").description("Proof of family income below ₹1,20,000/year").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Biometric identification of family members").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Visit CMCHIS District Kiosk at District Collectorate or Taluk Office.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Submit Smart Ration Card, Income Certificate, and complete biometric photo capture.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Receive instant CMCHIS Smart Health Card.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Present card at any network hospital for completely cashless medical care.").build()
                )
        );

        // --- MAHARASHTRA STATE SCHEMES ---

        // 24. Mukhyamantri Majhi Ladki Bahin Yojana - Maharashtra
        createSchemeIfMissing(
                catWomen,
                "Mukhyamantri Majhi Ladki Bahin Yojana",
                "Monthly financial aid of ₹1,500 for women aged 21-65 residing in Maharashtra.",
                "A transformative social assistance scheme providing ₹1,500 monthly direct cash transfers to economically weaker women residing in Maharashtra to foster financial autonomy and nutritional well-being.",
                "Women and Child Development Department, Govt of Maharashtra",
                "STATE",
                "Direct bank transfer of ₹1,500 per month (₹18,000 per year) credited to beneficiary's Aadhaar-linked account.",
                "https://ladkibahin.maharashtra.gov.in",
                "Women & Child Helpline: 181 | Support Desk: 022-22027050",
                EligibilityRule.builder()
                        .minAge(21)
                        .maxAge(65)
                        .gender("Female")
                        .maxIncome(250000.0)
                        .state("Maharashtra")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Identity and age proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Maharashtra Domicile Certificate / Ration Card").description("Proof of Maharashtra residence").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Yellow or Orange Ration Card / Income Certificate").description("Family annual income below ₹2.5 Lakh").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Bank Passbook with Aadhaar Link").description("Account for DBT payments").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Apply online via Nari Shakti Doot Mobile App or visit nearest Anganwadi / Setu Suvidha Kendra.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Upload Aadhaar, Maharashtra domicile proof, and bank passbook details.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Verification conducted by Child Development Project Officer (CDPO).").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Monthly ₹1,500 allowance deposited into bank account every month.").build()
                )
        );

        // 25. Mahatma Jyotirao Phule Jan Arogya Yojana (MJPJAY) - Maharashtra
        createSchemeIfMissing(
                catHealth,
                "Mahatma Jyotirao Phule Jan Arogya Yojana (MJPJAY)",
                "Cashless health insurance coverage up to ₹5,00,000 per family per year in Maharashtra.",
                "Universal health protection scheme for residents of Maharashtra providing cashless secondary and tertiary hospitalization covering 1,356 surgeries and medical treatments across empanelled network hospitals.",
                "Public Health Department, Govt of Maharashtra",
                "STATE",
                "100% cashless hospitalization up to ₹5 Lakh per family per year with free diagnostic investigations and medications.",
                "https://www.jeevandayee.gov.in",
                "MJPJAY Toll-Free Call Centre: 155388 / 1800-233-2200",
                EligibilityRule.builder()
                        .minAge(0)
                        .maxAge(110)
                        .maxIncome(300000.0)
                        .state("Maharashtra")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Yellow / Orange Ration Card").description("Proof of residency and income category in Maharashtra").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Biometric identity proof").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Meet the Arogyamitra at any empaneled government or private network hospital.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Present ration card and Aadhaar for instant eligibility verification.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Pre-authorization submitted electronically by hospital desk.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Undergo complete surgery or treatment with zero out-of-pocket expenses.").build()
                )
        );

        // 26. Namo Shetkari Mahasanman Nidhi Yojana - Maharashtra
        createSchemeIfMissing(
                catAgri,
                "Namo Shetkari Mahasanman Nidhi Yojana",
                "Annual cash assistance of ₹6,000 for landholding farmers in Maharashtra.",
                "State financial support program supplementing PM-KISAN by delivering an additional ₹6,000 per year in 3 equal installments of ₹2,000, ensuring a total annual support of ₹12,000 for Maharashtra farmers.",
                "Agriculture Department, Govt of Maharashtra",
                "STATE",
                "₹6,000 annual direct cash transfer credited in 3 installments of ₹2,000 before Kharif and Rabi seasons.",
                "https://krishi.maharashtra.gov.in",
                "Maharashtra Kisan Call Centre: 1800-180-1551",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(90)
                        .farmerStatus(true)
                        .maxIncome(400000.0)
                        .state("Maharashtra")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("7/12 Land Record Extract").description("Proof of agricultural landholding in Maharashtra").mandatory(true).build(),
                        RequiredDocument.builder().documentName("8A Holding Extract").description("Land parcel details").mandatory(true).build(),
                        RequiredDocument.builder().documentName("PM-KISAN Registration ID").description("Active beneficiary identifier").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Aadhaar-seeded bank account proof").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Farmers registered and verified under PM-KISAN in Maharashtra are auto-onboarded.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Ensure Aadhaar e-KYC and land seeding are verified on Maharashtra Agriculture portal.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Direct DBT installment credited into farmer bank account before each crop cycle.").build()
                )
        );

        // --- KARNATAKA STATE SCHEMES ---

        // 27. Karnataka Gruha Lakshmi Scheme
        createSchemeIfMissing(
                catWomen,
                "Karnataka Gruha Lakshmi Scheme",
                "Monthly financial grant of ₹2,000 for women family heads in Karnataka.",
                "Government of Karnataka social safety net program providing ₹2,000 monthly cash assistance to female heads of families holding BPL, Antyodaya, or APL ration cards across Karnataka.",
                "Department of Women and Child Development, Govt of Karnataka",
                "STATE",
                "₹2,000 monthly direct bank transfer (₹24,000 annually) deposited into the woman head's bank account.",
                "https://sevasindhu.karnataka.gov.in",
                "Gruha Lakshmi Helpline: 1902 | Support: 080-22279954",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(80)
                        .gender("Female")
                        .maxIncome(300000.0)
                        .state("Karnataka")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Karnataka Ration Card (BPL/AAY/APL)").description("Must specify woman as family head (Yajamani)").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card of Woman & Husband").description("Identity proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar-Seeded Bank Passbook").description("DBT receiving bank account").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Visit nearest Grama One, Karnataka One, or Bangalore One center or submit on Seva Sindhu.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Provide ration card number and Aadhaar details for instant OTP verification.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Receive official acknowledgment receipt with application number.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Monthly ₹2,000 credited directly into the beneficiary bank account.").build()
                )
        );

        // 28. Karnataka Yuva Nidhi Scheme
        createSchemeIfMissing(
                catEdu,
                "Karnataka Yuva Nidhi Scheme",
                "Unemployment stipend of ₹3,000/month for degree graduates and ₹1,500/month for diploma holders.",
                "State youth support initiative offering monthly unemployment allowances for up to 2 years to recent degree and diploma graduates domiciled in Karnataka while they seek employment or pursue vocational enhancement.",
                "Skill Development, Entrepreneurship and Livelihood Dept, Govt of Karnataka",
                "STATE",
                "₹3,000 monthly stipend for graduates, ₹1,500 for diploma holders for up to 24 months + free skill training.",
                "https://sevasindhugs.karnataka.gov.in",
                "Yuva Nidhi Call Centre: 1800-599-9918",
                EligibilityRule.builder()
                        .minAge(21)
                        .maxAge(28)
                        .occupation("Unemployed")
                        .employmentStatus("Unemployed")
                        .maxIncome(350000.0)
                        .state("Karnataka")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Degree / Diploma Certificate").description("Graduation marksheet from Karnataka recognized board").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Karnataka Domicile Certificate").description("Minimum 6 years residence in Karnataka").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Identity and age proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Unemployment Self-Declaration").description("Undertaking of not being in formal employment").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Register online on Seva Sindhu Yuva Nidhi portal.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Enter university degree registration number and complete Aadhaar e-KYC.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Submit monthly self-declaration of unemployment status online.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Monthly stipend credited directly into bank account.").build()
                )
        );

        // --- UTTAR PRADESH STATE SCHEMES ---

        // 29. Mukhyamantri Kanya Sumangala Yojana (UP)
        createSchemeIfMissing(
                catWomen,
                "Mukhyamantri Kanya Sumangala Yojana (UP)",
                "Financial assistance of ₹25,000 in 6 installments from birth to graduation for girl children in UP.",
                "A flagship initiative by the Government of Uttar Pradesh to eliminate female foeticide, promote education of girl children, and provide economic security from infant immunisation through higher education enrollment.",
                "Department of Women and Child Development, Govt of Uttar Pradesh",
                "STATE",
                "Conditional cash grant of ₹25,000 deposited in 6 stages into girl child's account.",
                "https://mksy.up.gov.in",
                "UP Women Helpline: 1090 / 181 | Directorate: 0522-2287234",
                EligibilityRule.builder()
                        .minAge(0)
                        .maxAge(25)
                        .gender("Female")
                        .maxIncome(300000.0)
                        .state("Uttar Pradesh")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Birth Certificate of Girl Child").description("Official birth registration").mandatory(true).build(),
                        RequiredDocument.builder().documentName("UP Domicile Certificate").description("Proof of permanent residence in Uttar Pradesh").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Family Income Certificate").description("Annual family income under ₹3 Lakh").mandatory(true).build(),
                        RequiredDocument.builder().documentName("School / College Admission Proof").description("Proof of active schooling or college admission").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Register citizen profile on mksy.up.gov.in.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Select applicable stage (Birth, Vaccination, Class 1, 6, 9, or College Admission).").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Verification carried out by Block Development Officer (BDO) or SDM.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Direct financial transfer credited to the child's bank account.").build()
                )
        );

        // 30. UP Gopalak Yojana (Dairy Loan Subsidy) - Uttar Pradesh
        createSchemeIfMissing(
                catAgri,
                "UP Gopalak Yojana Dairy Loan Subsidy",
                "Subsidized bank loan up to ₹9 Lakh with government interest subsidy for setting up dairy farms in UP.",
                "Provides self-employment opportunities to rural youth and dairy farmers in Uttar Pradesh by sanctioning bank loans for purchasing 5 to 10 milch cattle with government interest subvention support.",
                "Animal Husbandry Department, Govt of Uttar Pradesh",
                "STATE",
                "Bank loan up to ₹9,00,000 with government subsidy of ₹1,00,000 paid over 5 years (₹20,000/year).",
                "https://animalhusb.upsdc.gov.in",
                "UP Animal Husbandry Directorate: 0522-2740482",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(55)
                        .farmerStatus(true)
                        .maxIncome(300000.0)
                        .state("Uttar Pradesh")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Identity and age proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("UP Domicile Certificate").description("Proof of permanent residence in UP").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Dairy Management Training Certificate").description("Training proof from veterinary clinic or institute").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Land / Shed Ownership or Lease Deed").description("Proof of cattle housing facility").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Submit physical application form to the Chief Veterinary Officer (CVO) at District Veterinary Office.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("District Level Selection Committee reviews applicant credentials and conducts site inspection.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Application forwarded to nationalized bank for dairy loan approval.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Subsidy credited in 5 annual installments upon physical cattle audit.").build()
                )
        );

        // --- KERALA STATE SCHEMES ---

        // 31. Karunya Arogya Suraksha Padhathi (KASP) - Kerala
        createSchemeIfMissing(
                catHealth,
                "Karunya Arogya Suraksha Padhathi (KASP - Kerala)",
                "Cashless hospitalization coverage of ₹5 Lakh per family per year across Kerala hospitals.",
                "Comprehensive health cover implemented by State Health Agency Kerala guaranteeing cashless secondary and tertiary hospital treatments for vulnerable families across government medical colleges and empanelled private hospitals.",
                "Health and Family Welfare Department, Govt of Kerala",
                "STATE",
                "Up to ₹5,00,000 cashless medical and surgical care per family per year at all empanelled hospital centers.",
                "https://sha.kerala.gov.in",
                "Kerala State Health Agency Toll-Free: 1056 / 1800-425-1073",
                EligibilityRule.builder()
                        .minAge(0)
                        .maxAge(110)
                        .maxIncome(300000.0)
                        .state("Kerala")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Kerala Ration Card").description("Proof of family residence in Kerala").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Biometric identity proof").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Approach KASP Kiosk at any empanelled network hospital in Kerala.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Authenticate identity via biometric fingerprint or Aadhaar OTP.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Hospital Arogyamitra initiates cashless admission under KASP.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Receive full surgical and medical treatment with 100% cashless discharge.").build()
                )
        );

        // --- WEST BENGAL STATE SCHEMES ---

        // 32. Kanyashree Prakalpa - West Bengal
        createSchemeIfMissing(
                catEdu,
                "Kanyashree Prakalpa (West Bengal)",
                "Annual scholarship of ₹1,000 and one-time grant of ₹25,000 for girl students in West Bengal.",
                "United Nations Award-winning conditional cash transfer initiative by the Government of West Bengal promoting female secondary education, improving nutritional health, and incentivizing parents to delay child marriages until age 18.",
                "Department of Women and Child Development & Social Welfare, Govt of West Bengal",
                "STATE",
                "K1: Annual stipend of ₹1,000 (ages 13-18) + K2: One-time grant of ₹25,000 upon turning 18 if unmarried and enrolled in education.",
                "https://www.wbkanyashree.gov.in",
                "Kanyashree Support Desk: 033-23373840 | Email: support.kanyashree@nic.in",
                EligibilityRule.builder()
                        .minAge(13)
                        .maxAge(19)
                        .gender("Female")
                        .studentStatus(true)
                        .maxIncome(250000.0)
                        .state("West Bengal")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("School Enrollment Certificate").description("Proof of active enrollment in recognized WB school").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Unmarried Declaration signed by Parent").description("Confirmation of unmarried status").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Identity proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Bank Passbook in Girl's Name").description("Individual savings bank account").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Obtain Kanyashree application form (K1 or K2) from school or college office.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Submit completed form along with bank passbook and unmarried declaration to Headmaster.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("School uploads data directly to Kanyashree portal.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("DBT credited directly to girl's bank account on Kanyashree Divas.").build()
                )
        );

        // --- DELHI STATE SCHEMES ---

        // 33. Delhi Jai Bhim Mukhyamantri Pratibha Vikas Yojana
        createSchemeIfMissing(
                catEdu,
                "Delhi Jai Bhim Mukhyamantri Pratibha Vikas Yojana",
                "Free competitive exam coaching + ₹2,500 monthly stipend for Delhi students.",
                "Government of NCT of Delhi initiative providing 100% free coaching at top private coaching academies for civil services, engineering, medical, law, and banking examinations for talented students from underprivileged backgrounds.",
                "Department for Welfare of SC/ST/OBC, Govt of NCT of Delhi",
                "STATE",
                "100% free coaching course fees paid directly to empanelled institute + ₹2,500 monthly stipend paid to student.",
                "https://scstwelfare.delhi.gov.in",
                "Delhi SC/ST Welfare Dept Helpline: 011-23378418",
                EligibilityRule.builder()
                        .minAge(16)
                        .maxAge(30)
                        .studentStatus(true)
                        .maxIncome(800000.0)
                        .state("Delhi")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Delhi 10th & 12th Marksheet").description("Proof of passing from a school in Delhi").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Delhi Domicile / Voter ID / Aadhaar").description("Proof of residency in Delhi").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Income Certificate").description("Annual family income under ₹8 Lakh").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Caste / EWS Certificate").description("Category verification").mandatory(false).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Apply directly to any empanelled coaching institute in Delhi for your exam category.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Institute submits student application online on the Delhi SC/ST Welfare portal.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("Department conducts eligibility verification and sanctions admission.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Course fees settled directly with institute; ₹2,500 monthly stipend deposited into student account.").build()
                )
        );

        // --- GUJARAT STATE SCHEMES ---

        // 34. Mukhyamantri Kisan Sahay Yojana (Gujarat)
        createSchemeIfMissing(
                catAgri,
                "Mukhyamantri Kisan Sahay Yojana (Gujarat)",
                "Zero-premium crop disaster relief compensation of up to ₹25,000 per hectare for Gujarat farmers.",
                "Government of Gujarat disaster relief compensation program providing financial assistance to farmers suffering crop loss due to drought, excess rainfall, or unseasonal rain without any premium payment.",
                "Agriculture, Farmers Welfare & Co-operation Department, Govt of Gujarat",
                "STATE",
                "Compensation of ₹20,000 to ₹25,000 per hectare (max 4 hectares) for crop loss exceeding 33%.",
                "https://dag.gujarat.gov.in",
                "Gujarat Kisan Call Centre: 1800-180-1551",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(90)
                        .farmerStatus(true)
                        .state("Gujarat")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("7/12 & 8A Land Record Extracts").description("Land parcel details in Gujarat").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card").description("Identity proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Bank Passbook Copy").description("Bank account for compensation DBT").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Apply online at nearest E-Gram center via Digital Gujarat portal when calamity is notified.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Revenue Talati and Agriculture Officer verify crop damage survey report.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("District Collector approves sanction list.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Compensation amount credited directly to farmer's Aadhaar-linked bank account.").build()
                )
        );

        // --- ANDHRA PRADESH STATE SCHEMES ---

        // 35. YSR Rythu Bharosa - PM KISAN (Andhra Pradesh)
        createSchemeIfMissing(
                catAgri,
                "YSR Rythu Bharosa - PM KISAN (Andhra Pradesh)",
                "Annual financial input assistance of ₹13,500 for farmers in Andhra Pradesh.",
                "Flagship Andhra Pradesh farmer welfare program combining ₹7,500 state grant with ₹6,000 PM-KISAN grant to provide ₹13,500 annual input subsidy in 3 seasonal installments to support crop cultivation costs.",
                "Department of Agriculture, Govt of Andhra Pradesh",
                "STATE",
                "₹13,500 per farmer family per year disbursed in 3 installments before Kharif, Rabi, and harvest periods.",
                "https://ysrrythubharosa.ap.gov.in",
                "Rythu Bharosa Toll-Free: 155251 / 1902",
                EligibilityRule.builder()
                        .minAge(18)
                        .maxAge(90)
                        .farmerStatus(true)
                        .maxIncome(350000.0)
                        .state("Andhra Pradesh")
                        .build(),
                Arrays.asList(
                        RequiredDocument.builder().documentName("Land Pattadar Passbook / 1B Record").description("Proof of agricultural land in Andhra Pradesh").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Aadhaar Card of Farmer & Family").description("Identity and relationship proof").mandatory(true).build(),
                        RequiredDocument.builder().documentName("Bank Passbook").description("Aadhaar-seeded bank account for DBT").mandatory(true).build()
                ),
                Arrays.asList(
                        ApplicationProcedure.builder().stepNumber(1).instruction("Register at the local Rythu Bharosa Kendram (RBK) with Village Agriculture Assistant.").build(),
                        ApplicationProcedure.builder().stepNumber(2).instruction("Social audit beneficiary list displayed at Grama Sachivalayam for transparent public review.").build(),
                        ApplicationProcedure.builder().stepNumber(3).instruction("District Collector approves final verified list.").build(),
                        ApplicationProcedure.builder().stepNumber(4).instruction("Three seasonal installments credited directly to farmer's bank account before sowing cycles.").build()
                )
        );

        System.out.println(">>> Successfully Verified and Seeded Central & State Government Schemes Across Tamil Nadu, Maharashtra, Karnataka, UP, Kerala, West Bengal, Delhi, Gujarat & Andhra Pradesh!");
    }
}
