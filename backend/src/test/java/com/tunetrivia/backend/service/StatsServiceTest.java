package com.tunetrivia.backend.service;

import com.tunetrivia.backend.model.PlayStat;
import com.tunetrivia.backend.model.StatsSummary;
import com.tunetrivia.backend.repo.PlayStatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatsServiceTest {

    private PlayStatRepository repo;
    private StatsService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(PlayStatRepository.class);
        service = new StatsService(repo);
    }

    @Test
    void saveForUser_throwsUnauthorized_whenUserNull() {
        PlayStat stat = new PlayStat();
        assertThrows(ResponseStatusException.class, () -> service.saveForUser(null, stat));
    }

    @Test
    void saveForUser_throwsUnauthorized_whenUserBlank() {
        PlayStat stat = new PlayStat();
        assertThrows(ResponseStatusException.class, () -> service.saveForUser(" ", stat));
    }

    @Test
    void saveForUser_throwsBadRequest_whenStatNull() {
        assertThrows(ResponseStatusException.class, () -> service.saveForUser("user1", null));
    }

    @Test
    void saveForUser_normalizesAndSaves() {
        PlayStat input = new PlayStat();
        input.setGenre("");
        input.setTrackId("   ");
        input.setArtist("Artist Name");
        input.setTitle("Some Title");
        // repo.save should return the same object (simulate DB)
        when(repo.save(any(PlayStat.class))).thenAnswer(inv -> inv.getArgument(0));

        PlayStat saved = service.saveForUser("user-123", input);

        assertEquals("user-123", saved.getUserId());
        assertNotNull(saved.getPlayedAt(), "playedAt should be set when missing");
        assertNull(saved.getGenre(), "empty genre should be normalized to null");
        assertNull(saved.getTrackId(), "blank trackId should be normalized to null");
        assertEquals("Artist Name", saved.getArtist());
        verify(repo, times(1)).save(any(PlayStat.class));

        // capture the saved entity to ensure playedAt was set to a recent Instant
        ArgumentCaptor<PlayStat> captor = ArgumentCaptor.forClass(PlayStat.class);
        verify(repo).save(captor.capture());
        PlayStat captured = captor.getValue();
        assertEquals("user-123", captured.getUserId());
        assertTrue(captured.getPlayedAt().isBefore(Instant.now().plusSeconds(5)));
    }

    @Test
    void findByUser_returnsEmptyForBlankUser() {
        List<PlayStat> res = service.findByUser(" ");
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void findByUser_delegatesToRepo() {
        PlayStat p = new PlayStat();
        p.setId(1L);
        when(repo.findByUserId("u1")).thenReturn(List.of(p));

        List<PlayStat> res = service.findByUser("u1");
        assertEquals(1, res.size());
        assertEquals(1L, res.get(0).getId());
        verify(repo, times(1)).findByUserId("u1");
    }

    @Test
    void getSummaryForUser_returnsZeroForBlankUser() {
        StatsSummary sum = service.getSummaryForUser(" ");
        assertEquals(0, sum.getTotalPlays());
        assertEquals(0, sum.getTotalCorrect());
        assertEquals(0.0, sum.getAverageSkipsPerSong(), 0.0001);
    }

    @Test
    void getSummaryForUser_aggregatesRepoValues() {
        when(repo.countByUserId("u2")).thenReturn(10L);
        when(repo.countByUserIdAndCorrectTrue("u2")).thenReturn(7L);
        when(repo.findAverageSkipsPerSongByUser("u2")).thenReturn(1.5);

        StatsSummary sum = service.getSummaryForUser("u2");
        assertEquals(10L, sum.getTotalPlays());
        assertEquals(7L, sum.getTotalCorrect());
        assertEquals(1.5, sum.getAverageSkipsPerSong(), 0.0001);

        verify(repo, times(1)).countByUserId("u2");
        verify(repo, times(1)).countByUserIdAndCorrectTrue("u2");
        verify(repo, times(1)).findAverageSkipsPerSongByUser("u2");
    }

}
