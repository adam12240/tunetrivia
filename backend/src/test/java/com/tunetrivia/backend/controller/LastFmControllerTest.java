package com.tunetrivia.backend.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.tunetrivia.backend.service.LastFmService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(LastFmController.class)
@AutoConfigureMockMvc(addFilters = false)
class LastFmControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private LastFmService service;

    @Test
    @DisplayName("""
            GIVEN Last.fm returns fewer than 4 tracks
            WHEN GET /api/lastfm/top-tracks is called for a username
            THEN the controller replies 400 with lastfm_minimum_tracks error
            """)
    void topTracks_returnsBadRequest_whenTooFew() throws Exception {
        ArrayNode small = JsonNodeFactory.instance.arrayNode();
        Mockito.when(service.fetchTopTracks(eq("user1"), anyInt())).thenReturn(small);

        mvc.perform(get("/api/lastfm/top-tracks").param("user", "user1")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error", is("lastfm_minimum_tracks")));
    }

    @Test
    @DisplayName("""
            GIVEN Last.fm returns >=4 tracks
            WHEN GET /api/lastfm/top-tracks is called for a username
            THEN the controller returns 200 and the track array
            """)
    void topTracks_returnsOk_whenEnough() throws Exception {
        ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        arr.add(JsonNodeFactory.instance.objectNode().put("title", "T1"));
        arr.add(JsonNodeFactory.instance.objectNode().put("title", "T2"));
        arr.add(JsonNodeFactory.instance.objectNode().put("title", "T3"));
        arr.add(JsonNodeFactory.instance.objectNode().put("title", "T4"));
        Mockito.when(service.fetchTopTracks(eq("user2"), anyInt())).thenReturn(arr);

        mvc.perform(get("/api/lastfm/top-tracks").param("user", "user2")).andExpect(status().isOk()).andExpect(jsonPath("$[0].title", is("T1")));
    }

    @Test
    @DisplayName("""
            GIVEN a playlist URL that yields fewer than 4 tracks
            WHEN GET /api/lastfm/playlist?url=... is called
            THEN the controller responds 400 with lastfm_minimum_tracks
            """)
    void playlist_returnsBadRequest_whenTooFew() throws Exception {
        ArrayNode small = JsonNodeFactory.instance.arrayNode();
        Mockito.when(service.fetchPlaylistFromUrl(eq("http://p"), anyInt())).thenReturn(small);

        mvc.perform(get("/api/lastfm/playlist").param("url", "http://p")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error", is("lastfm_minimum_tracks")));
    }
}
