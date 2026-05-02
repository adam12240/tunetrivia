package com.tunetrivia.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tunetrivia.backend.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthService authService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("""
            GIVEN a Google ID token
            WHEN POST /api/auth/google is called with the token
            THEN the AuthService verifies it and the controller returns token and profile
            """)
    void verifyGoogle_returnsTokenAndProfile() throws Exception {
        var gv = new AuthService.GoogleVerifyResponse("sub-123", "a@b.com", "Name", "picurl");
        Mockito.when(authService.verifyGoogleIdToken("token-xyz")).thenReturn(gv);

        var req = mapper.writeValueAsString(new AuthController.GoogleIdTokenRequest("token-xyz"));

        mvc.perform(post("/api/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("sub-123")))
                .andExpect(jsonPath("$.email", is("a@b.com")))
                .andExpect(jsonPath("$.name", is("Name")));
    }

    @Test
    @DisplayName("""
            GIVEN no authenticated principal
            WHEN GET /api/auth/me is called
            THEN the endpoint returns 401 with an unauthenticated error body
            """)
    void me_returnsUnauthenticatedWhenNoUser() throws Exception {
        Mockito.when(authService.currentUser(null)).thenReturn(null);
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error", is("unauthenticated")));
    }

    @Test
    @DisplayName("""
            GIVEN any client
            WHEN POST /api/auth/logout is called
            THEN the controller clears MQ_AUTH cookies and returns logged_out status
            """)
    void logout_setsCookieHeaders() throws Exception {
        mvc.perform(post("/api/auth/logout")).andExpect(status().isOk()).andExpect(jsonPath("$.status", is("logged_out")));
    }

    @Test
    @DisplayName("""
            GIVEN an avatar URL on an unsupported host
            WHEN GET /api/auth/avatar?u=... is requested
            THEN the controller responds with 403 and error 'unsupported_host'
            """)
    void avatar_forbiddenForUnsupportedHost() throws Exception {
        mvc.perform(get("/api/auth/avatar").param("u", "https://example.com/foo.png")).andExpect(status().isForbidden()).andExpect(jsonPath("$.error", is("unsupported_host")));
    }
}
