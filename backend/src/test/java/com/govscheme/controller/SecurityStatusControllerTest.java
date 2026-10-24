package com.govscheme.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Security Status Endpoint Tests")
class SecurityStatusControllerTest {

    @Test
    @DisplayName("Security status endpoint reports BCrypt password hashing and HTTPS policies")
    void testSecurityStatusEndpoint() {
        AuthController controller = new AuthController();

        // 1. Test simulated HTTP request (Localhost Dev)
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setScheme("http");
        httpRequest.setSecure(false);
        httpRequest.setServerPort(8081);

        ResponseEntity<Map<String, Object>> httpResponse = controller.getSecurityStatus(httpRequest);
        assertNotNull(httpResponse.getBody());
        assertEquals("BCryptPasswordEncoder (Spring Security)", httpResponse.getBody().get("passwordHashing"));
        assertEquals(false, httpResponse.getBody().get("plainTextPasswordStorage"));
        assertEquals(false, httpResponse.getBody().get("isHttpsConnection"));

        // 2. Test simulated HTTPS request (Live Backend with SSL)
        MockHttpServletRequest httpsRequest = new MockHttpServletRequest();
        httpsRequest.setScheme("https");
        httpsRequest.setSecure(true);
        httpsRequest.addHeader("X-Forwarded-Proto", "https");
        httpsRequest.setServerPort(443);

        ResponseEntity<Map<String, Object>> httpsResponse = controller.getSecurityStatus(httpsRequest);
        assertNotNull(httpsResponse.getBody());
        assertEquals(true, httpsResponse.getBody().get("isHttpsConnection"));
        assertEquals("HTTPS (TLS Encrypted)", httpsResponse.getBody().get("transportProtocol"));
    }
}
