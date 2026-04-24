package com.tunetrivia.backend.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.tunetrivia.backend.service.LastFmService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/lastfm")
@RequiredArgsConstructor
public class LastFmController {
    private final LastFmService service;
    private final Logger log = LoggerFactory.getLogger(LastFmController.class);

    @GetMapping(value = "/top-tracks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> topTracks(@RequestParam(name = "user") String username,
                               @RequestParam(name = "limit", required = false, defaultValue = "50") int limit) {
        try {
            ArrayNode result = service.fetchTopTracks(username, limit);
            if (result == null || result.size() < 4) {
                ObjectNode err = JsonNodeFactory.instance.objectNode();
                err.put("error", "lastfm_minimum_tracks");
                err.put("detail", "Last.fm import must contain at least 4 tracks");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
            }
            return ResponseEntity.ok(result);
        } catch (ResponseStatusException ex) {
            // propagate known status with a JSON body {"error":"message"}
            ObjectNode err = JsonNodeFactory.instance.objectNode();
            err.put("error", ex.getReason() != null ? ex.getReason() : ex.getMessage());
            return ResponseEntity.status(ex.getStatusCode()).body(err);
        } catch (Exception ex) {
            // If this was an upstream IO issue, return 502
            log.error("Last.fm top-tracks failed", ex);
            ObjectNode err = JsonNodeFactory.instance.objectNode();
            err.put("error", "upstream_error");
            err.put("detail", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(err);
        }
    }

    @GetMapping(value = "/playlist", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> playlistFromUrl(@RequestParam(name = "url") String url,
                                             @RequestParam(name = "limit", required = false, defaultValue = "50") int limit) {
        try {
            ArrayNode result = service.fetchPlaylistFromUrl(url, limit);
            if (result == null || result.size() < 4) {
                ObjectNode err = JsonNodeFactory.instance.objectNode();
                err.put("error", "lastfm_minimum_tracks");
                err.put("detail", "Last.fm import must contain at least 4 tracks");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
            }
            return ResponseEntity.ok(result);
        } catch (ResponseStatusException ex) {
            ObjectNode err = JsonNodeFactory.instance.objectNode();
            err.put("error", ex.getReason() != null ? ex.getReason() : ex.getMessage());
            return ResponseEntity.status(ex.getStatusCode()).body(err);
        } catch (Exception ex) {
            log.error("Last.fm playlist failed", ex);
            ObjectNode err = JsonNodeFactory.instance.objectNode();
            err.put("error", "upstream_error");
            err.put("detail", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(err);
        }
    }
}
