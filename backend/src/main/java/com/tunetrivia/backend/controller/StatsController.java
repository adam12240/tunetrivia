package com.tunetrivia.backend.controller;

import com.tunetrivia.backend.model.PlayStat;
import com.tunetrivia.backend.model.StatsSummary;
import com.tunetrivia.backend.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Slf4j
public class StatsController {
    private final StatsService statsService;

    @PostMapping
    public ResponseEntity<?> saveStat(@Valid @RequestBody PlayStat stat, Authentication auth) {
        String userId = auth != null ? String.valueOf(auth.getPrincipal()) : null;
        var saved = statsService.saveForUser(userId, stat);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/me")
    public List<PlayStat> myStats(Authentication auth) {
        String userId = auth != null ? String.valueOf(auth.getPrincipal()) : null;
        return statsService.findByUser(userId);
    }

    @GetMapping("/me/summary")
    public ResponseEntity<StatsSummary> mySummary(Authentication auth) {
        String userId = auth != null ? String.valueOf(auth.getPrincipal()) : null;
        var summary = statsService.getSummaryForUser(userId);
        return ResponseEntity.ok(summary);
    }
}
