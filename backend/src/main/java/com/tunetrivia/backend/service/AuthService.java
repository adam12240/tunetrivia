package com.tunetrivia.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tunetrivia.backend.model.UserInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    // injected configuration for server-side OAuth flow
    private final String googleClientId;
    private final String googleClientSecret;
    private final Key signingKey; // may be null if no jwt secret configured

    public AuthService(@Value("${app.oauth2.google.clientId:}") String googleClientId,
                       @Value("${app.oauth2.google.clientSecret:}") String googleClientSecret,
                       @Value("${app.auth.jwtSecret:}") String jwtSecret) {
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } else {
            this.signingKey = null;
        }
    }

    public record GoogleVerifyResponse(String token, String email, String name, String picture) {}
    public record ExchangeResult(String backendToken, GoogleVerifyResponse verified) {}

    /**
     * Verify a Google ID token by calling Google's tokeninfo endpoint.
     * Also validates that the audience (aud) claim matches our Google Client ID.
     */
    public GoogleVerifyResponse verifyGoogleIdToken(String idToken) {
        try {
            var uri = URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + URLEncoder.encode(idToken, StandardCharsets.UTF_8));
            var req = HttpRequest.newBuilder().uri(uri).GET().build();
            var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.warn("Google tokeninfo returned status {} for token", resp.statusCode());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_token");
            }
            String body = resp.body();
            Map<String, Object> json = mapper.readValue(body, new TypeReference<>() {});

            // Validate audience (aud) matches our Google Client ID
            String aud = json.getOrDefault("aud", null) != null ? String.valueOf(json.get("aud")) : null;
            if (aud == null || aud.isBlank()) {
                log.warn("tokeninfo did not contain aud claim: {}", body);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "missing_aud_claim");
            }
            if (!aud.equals(googleClientId)) {
                log.warn("Token aud claim '{}' does not match our Google Client ID '{}'", aud, googleClientId);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "aud_mismatch");
            }

            String sub = json.getOrDefault("sub", null) != null ? String.valueOf(json.get("sub")) : null;
            String email = json.getOrDefault("email", null) != null ? String.valueOf(json.get("email")) : null;
            String name = json.getOrDefault("name", null) != null ? String.valueOf(json.get("name")) : null;
            String picture = json.getOrDefault("picture", null) != null ? String.valueOf(json.get("picture")) : null;

            if (sub == null || sub.isBlank()) {
                log.warn("tokeninfo did not contain sub: {}", body);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_token");
            }

            return new GoogleVerifyResponse(sub, email, name, picture);
        } catch (IOException e) {
            log.error("IO error while verifying token", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "upstream_io_error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Token verification interrupted", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "interrupted", e);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error verifying token", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error", e);
        }
    }

    /**
     * Exchange authorization code for tokens and optionally mint a backend JWT.
     */
    public ExchangeResult exchangeCodeForBackendToken(String code, String redirectUri) {
        if (googleClientId == null || googleClientId.isBlank() || googleClientSecret == null || googleClientSecret.isBlank()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "google_oauth_not_configured");
        }

        try {
            String body = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                    "&client_id=" + URLEncoder.encode(googleClientId, StandardCharsets.UTF_8) +
                    "&client_secret=" + URLEncoder.encode(googleClientSecret, StandardCharsets.UTF_8) +
                    "&grant_type=authorization_code" +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            var r = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() != 200) {
                log.warn("Token endpoint returned {}: {}", r.statusCode(), r.body());
                throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "token_exchange_failed");
            }

            Map<String, Object> json = mapper.readValue(r.body(), new TypeReference<>() {});
            String idToken = (String) json.get("id_token");
            if (idToken == null) throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "no_id_token");

            var verified = verifyGoogleIdToken(idToken);

            String backendToken = null;
            if (signingKey != null) {
                backendToken = Jwts.builder()
                        .setSubject(verified.token())
                        .claim("email", verified.email())
                        .claim("name", verified.name())
                        .claim("picture", verified.picture())
                        .setIssuedAt(Date.from(Instant.now()))
                        .setExpiration(Date.from(Instant.now().plusSeconds(60L * 60L * 24L * 7L)))
                        .signWith(signingKey)
                        .compact();
            }

            return new ExchangeResult(backendToken, verified);

        } catch (IOException e) {
            log.error("IO error exchanging code", e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "upstream_io_error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Exchange interrupted", e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "interrupted", e);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error exchanging code", e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "internal_error", e);
        }
    }

    /**
     * Handle an authorization code callback: perform exchange, verify id token, set MQ_AUTH cookie if backend token available,
     * and return the redirect URL to send the user back to the frontend.
     */
    public String handleAuthorizationCallback(String code, String redirectUri, jakarta.servlet.http.HttpServletResponse resp, String frontendUrl) {
        var result = exchangeCodeForBackendToken(code, redirectUri);
        String backendToken = result.backendToken();
        var verified = result.verified();

        if (backendToken != null) {
            // Force SameSite=None and Secure so the cookie is available cross-site after Google's redirect.
            int maxAge = 60 * 60 * 24 * 7;
            String cookie = "MQ_AUTH=" + backendToken + "; Path=/; HttpOnly; Max-Age=" + maxAge + "; SameSite=None; Secure";
            resp.addHeader("Set-Cookie", cookie);
        }

        // Build a frontend redirect that includes minimal user info for the client to pick up.
        try {
            String sub = verified.token() != null ? URLEncoder.encode(verified.token(), StandardCharsets.UTF_8) : "";
            String email = verified.email() != null ? URLEncoder.encode(verified.email(), StandardCharsets.UTF_8) : "";
            String name = verified.name() != null ? URLEncoder.encode(verified.name(), StandardCharsets.UTF_8) : "";
            String picture = verified.picture() != null ? URLEncoder.encode(verified.picture(), StandardCharsets.UTF_8) : "";
            StringBuilder sb = new StringBuilder(frontendUrl);
            if (!frontendUrl.contains("?")) sb.append('?'); else sb.append('&');
            sb.append("auth=success");
            if (!sub.isBlank()) sb.append("&sub=").append(sub);
            if (!email.isBlank()) sb.append("&email=").append(email);
            if (!name.isBlank()) sb.append("&name=").append(name);
            if (!picture.isBlank()) sb.append("&picture=").append(picture);
            return sb.toString();
        } catch (Exception e) {
            log.warn("Failed to build frontend redirect params", e);
            return frontendUrl;
        }
    }

    // Return a lightweight user info DTO from authentication (maybe null)
    public UserInfo currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Object principal = authentication.getPrincipal();
        String sub = null;
        String email = null;
        String name = null;
        String picture = null;
        List<String> roles;
        if (principal instanceof UserInfo u) {
            sub = u.getSub();
            email = u.getEmail();
            name = u.getName();
            picture = u.getPicture();
            roles = u.getRoles();
        } else if (principal != null) {
            sub = String.valueOf(principal);
            roles = authentication.getAuthorities() == null ? List.of() : authentication.getAuthorities().stream().map(Object::toString).collect(Collectors.toList());
        } else {
            roles = List.of();
        }
        return new UserInfo(sub, email, name, picture, roles);
    }
}
