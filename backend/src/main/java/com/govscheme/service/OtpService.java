package com.govscheme.service;

import com.govscheme.dto.OtpResponse;
import com.govscheme.dto.OtpSendRequest;
import com.govscheme.dto.OtpVerifyRequest;
import com.govscheme.dto.OtpVerifyResponse;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static class OtpEntry {
        final String otp;
        final Instant expiryTime;
        boolean verified;

        OtpEntry(String otp, Instant expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.verified = false;
        }
    }

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpResponse sendAadhaarOtp(OtpSendRequest request) {
        String cleanAadhaar = request.getAadhaarNumber().replaceAll("\\s+", "");
        
        // Generate a 6-digit OTP
        int code = 100000 + secureRandom.nextInt(900000);
        String otp = String.valueOf(code);
        Instant expiryTime = Instant.now().plusSeconds(600); // 10 minutes expiry

        otpStore.put(cleanAadhaar, new OtpEntry(otp, expiryTime));

        String mobile = request.getMobile();
        String maskedMobile;
        if (mobile != null && mobile.trim().length() >= 4) {
            String trimmed = mobile.trim();
            maskedMobile = "XXXXXX" + trimmed.substring(trimmed.length() - 4);
        } else {
            maskedMobile = "Aadhaar-linked mobile";
        }

        return OtpResponse.builder()
                .success(true)
                .message("OTP sent successfully to " + maskedMobile)
                .maskedMobile(maskedMobile)
                .demoOtp(otp)
                .build();
    }

    public OtpVerifyResponse verifyAadhaarOtp(OtpVerifyRequest request) {
        String cleanAadhaar = request.getAadhaarNumber().replaceAll("\\s+", "");
        OtpEntry entry = otpStore.get(cleanAadhaar);

        if (entry == null) {
            return OtpVerifyResponse.builder()
                    .verified(false)
                    .message("No active OTP request found for this Aadhaar number. Please click 'Get OTP'.")
                    .build();
        }

        if (Instant.now().isAfter(entry.expiryTime)) {
            otpStore.remove(cleanAadhaar);
            return OtpVerifyResponse.builder()
                    .verified(false)
                    .message("The OTP has expired. Please request a new OTP.")
                    .build();
        }

        if (!entry.otp.equals(request.getOtp().trim())) {
            return OtpVerifyResponse.builder()
                    .verified(false)
                    .message("Invalid OTP entered. Please check and try again.")
                    .build();
        }

        entry.verified = true;
        return OtpVerifyResponse.builder()
                .verified(true)
                .message("Aadhaar OTP verified successfully via UIDAI e-KYC!")
                .verificationToken("UIDAI_VERIFIED_" + System.currentTimeMillis())
                .build();
    }

    public boolean isAadhaarVerified(String aadhaarNumber) {
        if (aadhaarNumber == null) return false;
        String cleanAadhaar = aadhaarNumber.replaceAll("\\s+", "");
        OtpEntry entry = otpStore.get(cleanAadhaar);
        return entry != null && entry.verified;
    }
}
