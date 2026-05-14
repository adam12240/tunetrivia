package com.tunetrivia.backend.security;

import com.tunetrivia.backend.model.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.core.env.Environment;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final Key signingKey; // can be null -> plain-token mode
    private final boolean isDevelopment;

    public JwtAuthFilter(@Value("${app.auth.jwtSecret:}") String jwtSecret, Environment env) {
        this.isDevelopment = env.getActiveProfiles().length == 0 || java.util.Arrays.asList(env.getActiveProfiles()).contains("dev");

        if (jwtSecret != null && !jwtSecret.isBlank()) {
            try {
                this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                log.info("JwtAuthFilter configured to validate HS* tokens");
            } catch (Exception e) {
                log.warn("Failed to initialize signing key from jwtSecret, falling back to plain-token mode", e);
                throw new IllegalArgumentException("Invalid JWT secret configuration", e);
            }
        } else {
            this.signingKey = null;
            if (isDevelopment) {
                log.info("JwtAuthFilter running in plain-token mode (no jwtSecret configured) - DEVELOPMENT MODE ONLY");
            } else {
                log.error("JWT_SECRET not configured in production profile. Set app.auth.jwtSecret environment variable.");
                throw new IllegalArgumentException("JWT_SECRET must be configured in non-development profiles");
            }
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null && !token.isBlank()) {
                String subject = null;
                String email = null;
                String name = null;
                String picture = null;
                Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

                if (signingKey != null && looksLikeJwt(token)) {
                    try {
                        Jws<Claims> jws = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
                        Claims claims = jws.getBody();
                        subject = claims.getSubject();

                        Object roles = claims.get("roles");
                        if (roles != null) {
                            if (roles instanceof String) {
                                String[] parts = ((String) roles).split(",");
                                for (String p : parts) {
                                    String r = p.trim();
                                    if (!r.isEmpty()) authorities.add(new SimpleGrantedAuthority(r));
                                }
                            } else if (roles instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<Object> list = (List<Object>) roles;
                                for (Object o : list) if (o != null) authorities.add(new SimpleGrantedAuthority(String.valueOf(o)));
                            }
                        }

                        Object em = claims.get("email");
                        if (em != null) email = String.valueOf(em);
                        Object nm = claims.get("name");
                        if (nm != null) name = String.valueOf(nm);
                        Object pic = claims.get("picture");
                        if (pic != null) picture = String.valueOf(pic);

                    } catch (JwtException e) {
                        log.warn("Invalid backend JWT: {}", e.getMessage());
                        subject = null; // invalid token -> unauthenticated
                    }
                }

                // Plain-token mode or fallback: treat token as subject if no JWT parsing occurred
                if (subject == null) {
                    subject = token;
                    // basic role for plain token
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }

                UserInfo principal = new UserInfo(subject, email, name, picture, List.copyOf(authorities.stream().map(Object::toString).toList()));
                var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            log.error("Unexpected error in JwtAuthFilter", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        // check cookie MQ_AUTH
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("MQ_AUTH".equals(c.getName())) return c.getValue();
            }
        }
        return null;
    }

    private boolean looksLikeJwt(String token) {
        return token != null && token.chars().filter(ch -> ch == '.').count() == 2;
    }
}
