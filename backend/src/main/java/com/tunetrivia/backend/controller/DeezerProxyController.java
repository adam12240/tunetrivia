package com.tunetrivia.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tunetrivia.backend.service.DeezerProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class DeezerProxyController {
    private final DeezerProxyService service;

    @GetMapping("/deezer")
    public ResponseEntity<?> proxy(
            @RequestParam(required = false) String genreId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false, defaultValue = "150") int limit
    ) {
        try {
            if (genreId != null && !genreId.isBlank()) {
                int safeLimit = Math.max(1, Math.min(limit, 300));
                ObjectNode resp = service.fetchGenreTracks(genreId, safeLimit);
                return ResponseEntity.ok(resp);
            }
            String searchQuery = (genre != null && !genre.isBlank()) ? genre : q;
            if (searchQuery == null || searchQuery.isBlank()) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "Missing search query (?q=...) or genre (?genre=...) or genreId (?genreId=...)"));
            }
            JsonNode res = service.search(searchQuery);
            if (res == null) return ResponseEntity.status(500).body(java.util.Map.of("error", "Failed to fetch from Deezer API"));
            return ResponseEntity.ok(res);
        } catch (IOException | InterruptedException e) {
            log.error("Deezer proxy error", e);
            return ResponseEntity.status(500).body(java.util.Map.of("error", "Internal server error"));
        }
    }
}
