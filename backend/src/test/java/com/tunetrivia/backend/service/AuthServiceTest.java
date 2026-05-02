package com.tunetrivia.backend.service;

import com.tunetrivia.backend.model.UserInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Provide empty clientId/secret to avoid actual exchange during tests
        authService = new AuthService("", "", "");
    }

    @Test
    void currentUser_returnsNullForNullAuth() {
        assertNull(authService.currentUser(null));
    }

    @Test
    void currentUser_parsesUserInfoPrincipal() {
        UserInfo u = new UserInfo("sub1", "a@b.com", "Name", "pic", List.of("ROLE_USER"));
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(u);

        var got = authService.currentUser(auth);
        assertNotNull(got);
        assertEquals("sub1", got.getSub());
        assertEquals("a@b.com", got.getEmail());
    }

    @Test
    void currentUser_handlesStringPrincipal() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("principal-string");
        when(auth.getAuthorities()).thenReturn(null);

        var got = authService.currentUser(auth);
        assertNotNull(got);
        assertEquals("principal-string", got.getSub());
    }

    @Test
    void handleAuthorizationCallback_setsCookieAndReturnsRedirect() throws Exception {
        // Spy the service to stub exchangeCodeForBackendToken
        AuthService spy = Mockito.spy(new AuthService("cid", "secret", "01234567890123456789012345678901"));
        AuthService.GoogleVerifyResponse gv = new AuthService.GoogleVerifyResponse("sub-val", "e@e.com", "Name", "picurl");
        AuthService.ExchangeResult ex = new AuthService.ExchangeResult("backend-token-xyz", gv);
        doReturn(ex).when(spy).exchangeCodeForBackendToken(eq("code123"), anyString());

        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        String redirect = spy.handleAuthorizationCallback("code123", "http://localhost/callback", resp, "http://frontend.local");

        // Verify Set-Cookie header was added
        verify(resp, atLeastOnce()).addHeader(eq("Set-Cookie"), contains("MQ_AUTH="));
        // Redirect URL should contain auth=success and encoded sub/email/name/picture
        assertTrue(redirect.startsWith("http://frontend.local"));
        assertTrue(redirect.contains("auth=success"));
        assertTrue(redirect.contains("sub=" + URLEncoder.encode("sub-val", StandardCharsets.UTF_8)));
        assertTrue(redirect.contains("email=" + URLEncoder.encode("e@e.com", StandardCharsets.UTF_8)));
    }
}
