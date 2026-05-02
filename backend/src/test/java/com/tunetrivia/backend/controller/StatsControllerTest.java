package com.tunetrivia.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tunetrivia.backend.model.PlayStat;
import com.tunetrivia.backend.model.StatsSummary;
import com.tunetrivia.backend.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(StatsController.class)
@AutoConfigureMockMvc(addFilters = false)
class StatsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StatsService statsService;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setup() {
    }

    @Test
    @DisplayName("""
            GIVEN an authenticated user
            WHEN they POST a PlayStat to /api/stats
            THEN the stat is delegated to StatsService and the saved entity is returned
            """)
    void saveStat_requiresAuthentication_andDelegates() throws Exception {
        PlayStat p = new PlayStat();
        p.setArtist("Artist");
        p.setTitle("Title");
        p.setPlayedAt(Instant.now());
        Mockito.when(statsService.saveForUser(eq("sub-1"), any(PlayStat.class))).thenReturn(p);

        // Simulate authentication principal using spring security's principal as string
        var auth = new UsernamePasswordAuthenticationToken("sub-1", null, Collections.emptyList());
        mvc.perform(post("/api/stats").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(p)).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artist", is("Artist")));
    }

    @Test
    @DisplayName("""
            GIVEN a user with saved stats
            WHEN they request GET /api/stats/me
            THEN a list of their PlayStat entries is returned
            """)
    void myStats_returnsListForUser() throws Exception {
        PlayStat p = new PlayStat();
        p.setId(1L);
        Mockito.when(statsService.findByUser("sub-2")).thenReturn(List.of(p));

        var auth2 = new UsernamePasswordAuthenticationToken("sub-2", null, Collections.emptyList());
        mvc.perform(get("/api/stats/me").principal(auth2)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @DisplayName("""
            GIVEN a user with play statistics
            WHEN they request GET /api/stats/me/summary
            THEN a StatsSummary with aggregated values is returned
            """)
    void mySummary_returnsStatsSummary() throws Exception {
        Mockito.when(statsService.getSummaryForUser("sub-3")).thenReturn(new StatsSummary(10,7,1.5));
        var auth3 = new UsernamePasswordAuthenticationToken("sub-3", null, Collections.emptyList());
        mvc.perform(get("/api/stats/me/summary").principal(auth3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPlays", is(10)));
    }
}
