package com.tunetrivia.backend.controller;

import com.tunetrivia.backend.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/api/auth")
public class OAuthController {

    private final AuthService authService;
    private final String clientId;
    private final String frontendUrl;
    private final String redirectUri;
    private static final Logger log = LoggerFactory.getLogger(OAuthController.class);

    public OAuthController(AuthService authService,
                           @Value("${app.oauth2.google.clientId:}") String clientId,
                           @Value("${app.frontend.url:http://localhost:5173}") String frontendUrl,
                           @Value("${app.oauth2.google.redirectUri:http://localhost:3002/api/auth/callback}") String redirectUri) {
        this.authService = authService;
        this.clientId = clientId;
        this.frontendUrl = frontendUrl;
        this.redirectUri = redirectUri;
    }

    @GetMapping("/start")
    public void start(HttpServletResponse resp) throws Exception {
        if (clientId == null || clientId.isBlank()) throw new IllegalStateException("Google client id not configured");
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedScope = URLEncoder.encode("openid email profile", StandardCharsets.UTF_8);
        String uri = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?response_type=code" +
                "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&scope=" + encodedScope +
                "&redirect_uri=" + encodedRedirect +
                "&prompt=select_account";
        log.info("Redirecting to Google OAuth endpoint, redirect_uri={} (encoded={})", redirectUri, encodedRedirect);
        resp.sendRedirect(uri);
    }

    @GetMapping(value = "/callback", produces = MediaType.TEXT_HTML_VALUE)
    public void callback(@RequestParam(name = "code", required = false) String code,
                         @RequestParam(name = "error", required = false) String error,
                         HttpServletResponse resp) throws Exception {
        if (error != null) {
            // Redirect back to frontend without exposing error details in the URL.
            resp.sendRedirect(frontendUrl);
            return;
        }
        if (code == null) {
            resp.sendError(400, "missing code");
            return;
        }

        // delegate entire callback handling to the service (exchange, verification, cookie setting)
        String target = authService.handleAuthorizationCallback(code, redirectUri, resp, frontendUrl);
        resp.sendRedirect(target);
    }
}
