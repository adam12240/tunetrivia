package com.tunetrivia.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DeezerProxyService {
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Pattern nonAscii = Pattern.compile("[^\\x00-\\x7F]");

    public ObjectNode fetchGenreTracks(String genreId, int maxQuizSize) throws IOException, InterruptedException {
        String apiUrl = String.format("https://api.deezer.com/chart/%s/tracks?limit=300", URLEncoder.encode(genreId, StandardCharsets.UTF_8));
        JsonNode root = fetchJson(apiUrl);
        ArrayNode data = mapper.createArrayNode();
        if (root != null && root.has("data") && root.get("data").isArray()) {
            for (JsonNode item : root.get("data")) data.add(item);
        }

        List<JsonNode> filtered = new ArrayList<>();
        for (JsonNode track : data) {
            String title = safeText(track.at("/title"));
            String artist = safeText(track.at("/artist/name"));
            if (title == null || artist == null) continue;
            if (nonAscii.matcher(title).find() || nonAscii.matcher(artist).find()) continue;
            filtered.add(track);
        }

        Collections.shuffle(filtered);
        List<JsonNode> quizTracks = filtered.stream().limit(maxQuizSize).toList();

        LinkedHashMap<String, JsonNode> artistMap = new LinkedHashMap<>();
        for (JsonNode t : filtered) {
            JsonNode art = t.path("artist");
            if (art != null && art.has("name")) {
                String name = art.path("name").asText();
                artistMap.putIfAbsent(name, art);
            }
        }

        ArrayNode topArtists = mapper.createArrayNode();
        artistMap.values().forEach(topArtists::add);

        ObjectNode artistTracks = mapper.createObjectNode();
        for (JsonNode t : filtered) {
            String artistName = safeText(t.at("/artist/name"));
            if (artistName == null) continue;
            ArrayNode arr = (ArrayNode) artistTracks.get(artistName);
            if (arr == null) {
                arr = mapper.createArrayNode();
                artistTracks.set(artistName, arr);
            }
            ObjectNode entry = mapper.createObjectNode();
            entry.put("title", safeText(t.at("/title")));
            entry.put("release_date", safeText(t.at("/release_date")));
            arr.add(entry);
        }

        ObjectNode resp = mapper.createObjectNode();
        ArrayNode quizArray = mapper.createArrayNode();
        quizTracks.forEach(quizArray::add);
        resp.set("data", quizArray);
        resp.set("topArtists", topArtists);
        resp.set("artistTracks", artistTracks);
        return resp;
    }

    public JsonNode search(String query) throws IOException, InterruptedException {
        String apiURL = "https://api.deezer.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        return fetchJson(apiURL);
    }

    private JsonNode fetchJson(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return mapper.readTree(resp.body());
        }
        log.warn("Deezer API returned status {} for {}", resp.statusCode(), url);
        return null;
    }

    private String safeText(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        return node.asText(null);
    }
}
