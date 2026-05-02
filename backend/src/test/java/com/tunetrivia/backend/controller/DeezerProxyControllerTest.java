package com.tunetrivia.backend.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.tunetrivia.backend.service.DeezerProxyService;
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

@WebMvcTest(DeezerProxyController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeezerProxyControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DeezerProxyService service;

    @Test
    @DisplayName("""
            GIVEN no query and no genre and no genreId
            WHEN GET /deezer is called
            THEN the controller responds 400 with a helpful error message
            """)
    void proxy_returnsBadRequest_whenNoQueryOrGenre() throws Exception {
        mvc.perform(get("/deezer")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error", is("Missing search query (?q=...) or genre (?genre=...) or genreId (?genreId=...)")));
    }

    @Test
    @DisplayName("""
            GIVEN a valid genreId
            WHEN GET /deezer?genreId=... is called
            THEN the controller returns genre tracks from the service
            """)
    void proxy_returnsGenreTracks_whenGenreId() throws Exception {
        ObjectNode n = JsonNodeFactory.instance.objectNode();
        n.put("genre", "pop");
        Mockito.when(service.fetchGenreTracks(eq("123"), anyInt())).thenReturn(n);

        mvc.perform(get("/deezer").param("genreId", "123")).andExpect(status().isOk()).andExpect(jsonPath("$.genre", is("pop")));
    }

    @Test
    @DisplayName("""
            GIVEN a search query
            WHEN GET /deezer?q=... is called
            THEN the controller forwards the search to the DeezerProxyService and returns results
            """)
    void proxy_returnsSearchResults_whenQuery() throws Exception {
        ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        arr.add(TextNode.valueOf("x"));
        Mockito.when(service.search(eq("abc"))).thenReturn(arr);

        mvc.perform(get("/deezer").param("q", "abc")).andExpect(status().isOk()).andExpect(jsonPath("$[0]", is("x")));
    }
}
