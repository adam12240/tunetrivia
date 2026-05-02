package com.tunetrivia.backend.service;

import com.tunetrivia.backend.model.PlayStat;
import com.tunetrivia.backend.repo.PlayStatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatsServiceUnitTest {

    private PlayStatRepository repo;
    private StatsService service;

    @BeforeEach
    void init() {
        repo = Mockito.mock(PlayStatRepository.class);
        service = new StatsService(repo);
    }

    @Test
    void saveForUser_requiresUserAndStat() {
        PlayStat s = new PlayStat();
        assertThrows(ResponseStatusException.class, () -> service.saveForUser(null, s));
        assertThrows(ResponseStatusException.class, () -> service.saveForUser("user1", null));
    }

    @Test
    void saveForUser_normalizesFieldsAndCallsRepo() {
        PlayStat s = new PlayStat();
        s.setGenre("");
        s.setTrackId("   ");
        s.setArtist("Artist");
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PlayStat saved = service.saveForUser("u1", s);

        assertEquals("u1", saved.getUserId());
        assertNotNull(saved.getPlayedAt());
        assertNull(saved.getGenre());
        assertNull(saved.getTrackId());
        verify(repo, times(1)).save(any());

        ArgumentCaptor<PlayStat> cap = ArgumentCaptor.forClass(PlayStat.class);
        verify(repo).save(cap.capture());
        PlayStat capVal = cap.getValue();
        assertTrue(capVal.getPlayedAt().isBefore(Instant.now().plusSeconds(5)));
    }

    @Test
    void findByUser_blankReturnsEmpty() {
        var res = service.findByUser(" ");
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }
}

