package com.tunetrivia.backend.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tunetrivia.backend.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    // Simple request DTO with validation
    public record GoogleIdTokenRequest(@JsonProperty("idToken") @NotBlank String idToken) {}

    @PostMapping(value = "/google", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verifyGoogle(@Valid @RequestBody GoogleIdTokenRequest req) {
        var result = authService.verifyGoogleIdToken(req.idToken());
        return ResponseEntity.ok(Map.of("token", result.token(), "email", result.email(), "name", result.name()));
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> me(Authentication authentication) {
        var me = authService.currentUser(authentication);
        if (me == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "unauthenticated"));
        return ResponseEntity.ok(me);
    }

    @PostMapping(value = "/logout")
    public ResponseEntity<?> logout(HttpServletResponse resp) {
        // clear the MQ_AUTH cookie by setting Max-Age=0 and including SameSite=None; Secure to match how it was set
        String cookieValueSecure = "MQ_AUTH=; Path=/; HttpOnly; Max-Age=0; SameSite=None; Secure";
        String cookieValueInsecure = "MQ_AUTH=; Path=/; HttpOnly; Max-Age=0; SameSite=None";
        resp.addHeader("Set-Cookie", cookieValueSecure);
        resp.addHeader("Set-Cookie", cookieValueInsecure);
        return ResponseEntity.ok(Map.of("status", "logged_out"));
    }

    private final HttpClient client = HttpClient.newHttpClient();

    @GetMapping(value = "/avatar")
    public ResponseEntity<?> avatar(@RequestParam("u") String url) {
        try {
            // Basic validation: only allow googleusercontent images for now
            var uri = URI.create(url);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
            if (!host.endsWith("googleusercontent.com") && !host.endsWith("ggpht.com") && !host.contains("google.com")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "unsupported_host"));
            }
            var req = HttpRequest.newBuilder().uri(uri).GET().build();
            var resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() != 200) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", "upstream", "status", resp.statusCode()));
            }
            // determine content-type
            String ct = resp.headers().firstValue("content-type").orElse("image/*");
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(ct)).body(resp.body());
        } catch (Exception e) {
            log.warn("avatar proxy error for url {}", url, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", "proxy_error"));
        }
    }

}
