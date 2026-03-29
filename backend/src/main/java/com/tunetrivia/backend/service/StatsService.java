package com.tunetrivia.backend.service;

import com.tunetrivia.backend.model.PlayStat;
import com.tunetrivia.backend.model.StatsSummary;
import com.tunetrivia.backend.repo.PlayStatRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {
    private static final Logger log = LoggerFactory.getLogger(StatsService.class);

    private final PlayStatRepository repo;

    public PlayStat saveForUser(String userId, PlayStat stat) {
        if (userId == null || userId.isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (stat == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty_payload");

        if (!StringUtils.hasText(stat.getGenre())) stat.setGenre(null);
        if (!StringUtils.hasText(stat.getTrackId())) stat.setTrackId(null);
        if (!StringUtils.hasText(stat.getArtist())) stat.setArtist(null);
        if (!StringUtils.hasText(stat.getTitle())) stat.setTitle(null);

        stat.setUserId(userId);
        if (stat.getPlayedAt() == null) stat.setPlayedAt(Instant.now());

        var saved = repo.save(stat);
        log.debug("Saved PlayStat {} for user {}", saved.getId(), userId);
        return saved;
    }

    public List<PlayStat> findByUser(String userId) {
        if (userId == null || userId.isBlank()) return List.of();
        return repo.findByUserId(userId);
    }

    public StatsSummary getSummaryForUser(String userId) {
        if (userId == null || userId.isBlank()) return new StatsSummary(0,0,0.0);
        long totalPlays = repo.countByUserId(userId);
        long totalCorrect = repo.countByUserIdAndCorrectTrue(userId);
        Double avgSkipsPerSongObj = repo.findAverageSkipsPerSongByUser(userId);
        double avgSkipsPerSong = (avgSkipsPerSongObj == null) ? 0.0 : avgSkipsPerSongObj.doubleValue();
        return new StatsSummary(totalPlays, totalCorrect, avgSkipsPerSong);
    }
}
