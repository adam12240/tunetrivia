package com.tunetrivia.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LastFmServiceUnitTest {

    private DeezerProxyService deezerProxyService;
    private LastFmService lastFmService;

    @BeforeEach
    void setup() {
        deezerProxyService = Mockito.mock(DeezerProxyService.class);
        lastFmService = new LastFmService(deezerProxyService);
    }

    @Test
    void fetchTopTracks_missingUsernameThrows() {
        assertThrows(ResponseStatusException.class, () -> lastFmService.fetchTopTracks(null, 10));
        assertThrows(ResponseStatusException.class, () -> lastFmService.fetchTopTracks(" ", 10));
    }

    @Test
    void fetchTopTracks_missingApiKeyThrows() {
        // Ensure lastfmApiKey is blank by default; method should throw internal server error
        assertThrows(ResponseStatusException.class, () -> lastFmService.fetchTopTracks("someuser", 5));
    }

    @Test
    void fetchPlaylistFromUrl_missingUrlThrows() {
        assertThrows(ResponseStatusException.class, () -> lastFmService.fetchPlaylistFromUrl(null, 10));
        assertThrows(ResponseStatusException.class, () -> lastFmService.fetchPlaylistFromUrl(" ", 10));
    }
}

