package com.tunetrivia.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LastFmService {
    private final ObjectMapper mapper = new ObjectMapper();
    // Use a client that follows redirects and has a connect timeout to avoid 3xx responses leaking back
    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final DeezerProxyService deezerProxyService;

    @Value("${app.lastfm.apiKey:}")
    private String lastfmApiKey;

    private String unescape(String s) {
        if (s == null) return null;
        return StringEscapeUtils.unescapeHtml4(s).trim();
    }

    // Fetch top tracks for a Last.fm username and try to map to Deezer previews
    public ArrayNode fetchTopTracks(String username, int limit) {
        if (username == null || username.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "missing_username");
        if (lastfmApiKey == null || lastfmApiKey.isBlank()) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "lastfm_not_configured");
        try {
            String url = "http://ws.audioscrobbler.com/2.0/?method=user.gettoptracks&user=" + URLEncoder.encode(username, StandardCharsets.UTF_8) + "&api_key=" + URLEncoder.encode(lastfmApiKey, StandardCharsets.UTF_8) + "&format=json&limit=" + limit + "&period=overall";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "TuneTrivia/1.0")
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.warn("Last.fm returned status {} for user.getTopTracks: {}", resp.statusCode(), resp.body());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "lastfm_error");
            }
            JsonNode root = mapper.readTree(resp.body());
            JsonNode toptracks = root.path("toptracks").path("track");
            ArrayNode out = mapper.createArrayNode();
            if (toptracks == null || toptracks.isMissingNode()) return out;
            int count = 0;
            Iterator<JsonNode> it = toptracks.elements();
            while (it.hasNext() && count < limit) {
                JsonNode t = it.next();
                String title = unescape(t.path("name").asText(null));
                String artist = unescape(t.path("artist").path("name").asText(null));
                // mbid not used for now
                String image = null;
                if (t.has("image") && t.path("image").isArray()) {
                    for (JsonNode img : t.path("image")) {
                        if (img.path("size").asText().equalsIgnoreCase("extralarge") || img.path("size").asText().equalsIgnoreCase("large")) {
                            image = img.path("#text").asText(null);
                        }
                    }
                    if (image == null && !t.path("image").isEmpty()) image = t.path("image").get(0).path("#text").asText(null);
                }

                ObjectNode entry = mapper.createObjectNode();
                entry.put("title", title != null ? title : "");
                ObjectNode artObj = mapper.createObjectNode();
                artObj.put("name", artist != null ? artist : "");
                entry.set("artist", artObj);
                ObjectNode albumObj = mapper.createObjectNode();
                if (image != null) albumObj.put("cover_xl", image);
                entry.set("album", albumObj);

                // Try to map to Deezer preview by searching artist + title
                try {
                    String q = (artist != null ? artist : "") + " " + (title != null ? title : "");
                    JsonNode deezerResp = deezerProxyService.search(q);
                    if (deezerResp != null && deezerResp.has("data") && deezerResp.path("data").isArray() && !deezerResp.path("data").isEmpty()) {
                        JsonNode d = deezerResp.path("data").get(0);
                        entry.put("deezer_id", d.path("id").asText(""));
                        entry.put("preview", d.path("preview").asText(null));
                        if (!entry.has("album") || entry.path("album").isMissingNode()) entry.set("album", mapper.createObjectNode());
                        ((ObjectNode) entry.path("album")).put("cover_xl", d.path("album").path("cover_xl").asText(null));
                    } else {
                        entry.put("preview", (String) null);
                    }
                } catch (Exception e) {
                    log.debug("Deezer mapping failed for {} - {}: {}", artist, title, e.getMessage());
                    entry.put("preview", (String) null);
                }

                out.add(entry);
                count++;
            }
            return out;
        } catch (IOException e) {
            log.error("IO error fetching Last.fm top tracks", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "upstream_io_error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "interrupted", e);
        }
    }

    // Fetch a public Last.fm playlist page (URL) and extract its ordered tracks into an ArrayNode
    public ArrayNode fetchPlaylistFromUrl(String playlistUrl, int limit) {
        if (playlistUrl == null || playlistUrl.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "missing_playlist_url");
        try {
            // Use a realistic browser User-Agent and Accept headers to avoid Last.fm blocking the request
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(playlistUrl))
                    .GET()
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                String snippet = resp.body() == null ? "" : resp.body().length() > 800 ? resp.body().substring(0, 800) : resp.body();
                log.warn("Failed to fetch playlist page {}: status {}. Body snippet: {}", playlistUrl, resp.statusCode(), snippet.replaceAll("\\n", " ").replaceAll("\\s+", " "));
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "lastfm_error");
            }
            String html = resp.body();
            // Try to extract tracks by scanning <tr class="chartlist-row"> rows and picking title/artist from TDs
            Pattern rowPat = Pattern.compile("(?is)<tr[^>]*class=\"[^\"]*chartlist-row[^\"]*\".*?</tr>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher rm = rowPat.matcher(html);
            List<String[]> pairs = new ArrayList<>();
            Pattern titleInRow = Pattern.compile("(?is)<td[^>]*class=\"[^\"]*chartlist-name[^\"]*\".*?<a[^>]*>([^<]+)</a>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Pattern artistInRow = Pattern.compile("(?is)<td[^>]*class=\"[^\"]*chartlist-artist[^\"]*\".*?<a[^>]*>([^<]+)</a>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            while (rm.find() && pairs.size() < limit) {
                String row = rm.group(0);
                Matcher tmat = titleInRow.matcher(row);
                Matcher amat = artistInRow.matcher(row);
                String t = null, a = null;
                if (tmat.find()) t = unescape(tmat.group(1).trim());
                if (amat.find()) a = unescape(amat.group(1).trim());
                if (t != null && a != null) pairs.add(new String[]{t, a});
            }

            // If still empty, attempt to find JSON-LD structured data (application/ld+json)
            if (pairs.isEmpty()) {
                // Correctly-escaped regex for Java string; capture JSON-LD blocks
                Pattern ld = Pattern.compile("<script[^>]*type=\\\"application/ld\\\\+json\\\"[^>]*>([\\\\s\\\\S]*?)</script>", Pattern.CASE_INSENSITIVE);
                Matcher ldm = ld.matcher(html);
                while (ldm.find() && pairs.size() < limit) {
                    String json = ldm.group(1);
                    try {
                        JsonNode root = mapper.readTree(json);
                        if (root.isObject() && root.has("track")) {
                            JsonNode tracks = root.path("track");
                            if (tracks.isArray()) {
                                for (JsonNode t : tracks) {
                                    if (pairs.size() >= limit) break;
                                    String ttitle = unescape(t.path("name").asText(null));
                                    String tart = unescape(t.path("byArtist").path("name").asText(null));
                                    if (ttitle != null && tart != null) pairs.add(new String[]{ttitle, tart});
                                }
                                break;
                            }
                        }
                    } catch (Exception ignore) { }
                }
            }

            // If still empty, try fetching JSON endpoints that Last.fm pages often use to load playlist data
            if (pairs.isEmpty()) {
                List<String> candidates = new ArrayList<>();
                candidates.add(playlistUrl + "?format=json");
                candidates.add(playlistUrl + "/+tracks?format=json");
                candidates.add(playlistUrl + "/tracks?format=json");
                candidates.add(playlistUrl + ".json");
                // try to detect /user/{user}/playlists/{id} pattern and build an explicit +tracks URL
                try {
                    java.util.regex.Matcher m = Pattern.compile("/user/([^/]+)/playlists/(\\d+)", Pattern.CASE_INSENSITIVE).matcher(playlistUrl);
                    if (m.find()) {
                        String user = m.group(1);
                        String id = m.group(2);
                        candidates.add("https://www.last.fm/user/" + URLEncoder.encode(user, StandardCharsets.UTF_8) + "/playlists/" + id + "/+tracks?format=json");
                    }
                } catch (Exception ignored) {}

                for (String cUrl : candidates) {
                    if (pairs.size() >= limit) break;
                    try {
                        HttpRequest rq = HttpRequest.newBuilder().uri(URI.create(cUrl)).GET().timeout(Duration.ofSeconds(12)).header("User-Agent", "TuneTrivia/1.0").build();
                        HttpResponse<String> rr = client.send(rq, HttpResponse.BodyHandlers.ofString());
                        if (rr.statusCode() != 200) continue;
                        String body = rr.body();
                        JsonNode root = null;
                        try { root = mapper.readTree(body); } catch (Exception ex) { continue; }
                        // look for common array locations
                        JsonNode tracksNode = null;
                        if (root.has("playlist") && root.path("playlist").has("tracks")) tracksNode = root.path("playlist").path("tracks");
                        else if (root.has("tracks")) tracksNode = root.path("tracks");
                        else if (root.has("entries")) tracksNode = root.path("entries");
                        else if (root.has("tracks") && root.path("tracks").isArray()) tracksNode = root.path("tracks");
                        else if (root.isArray()) tracksNode = root;

                        if (tracksNode != null && tracksNode.isArray() && tracksNode.size() > 0) {
                            for (JsonNode t : tracksNode) {
                                if (pairs.size() >= limit) break;
                                String ttitle = null;
                                String tart = null;
                                if (t.has("name")) ttitle = unescape(t.path("name").asText(null));
                                if (t.has("artist") && t.path("artist").isTextual()) tart = unescape(t.path("artist").asText(null));
                                if (t.has("artist") && t.path("artist").isObject()) tart = unescape(t.path("artist").path("name").asText(null));
                                if (ttitle == null && t.has("title")) ttitle = unescape(t.path("title").asText(null));
                                if (ttitle != null && tart != null) pairs.add(new String[]{ttitle, tart});
                            }
                            if (!pairs.isEmpty()) break;
                        }
                    } catch (Exception ignored) { }
                }
            }

            ArrayNode out = mapper.createArrayNode();
            Set<String> seen = new LinkedHashSet<>();
            for (String[] p : pairs) {
                if (out.size() >= limit) break;
                String title = p[0];
                String artist = p[1];
                String key = (artist != null ? artist.toLowerCase() : "") + "|" + (title != null ? title.toLowerCase() : "");
                if (seen.contains(key)) continue;
                seen.add(key);

                ObjectNode entry = mapper.createObjectNode();
                entry.put("title", title != null ? title : "");
                ObjectNode artObj = mapper.createObjectNode();
                artObj.put("name", artist != null ? artist : "");
                entry.set("artist", artObj);
                entry.set("album", mapper.createObjectNode());

                try {
                    String q = (artist != null ? artist : "") + " " + (title != null ? title : "");
                    JsonNode deezerResp = deezerProxyService.search(q);
                    if (deezerResp != null && deezerResp.has("data") && deezerResp.path("data").isArray() && !deezerResp.path("data").isEmpty()) {
                        JsonNode d = deezerResp.path("data").get(0);
                        entry.put("deezer_id", d.path("id").asText(""));
                        entry.put("preview", d.path("preview").asText(null));
                        ((ObjectNode) entry.path("album")).put("cover_xl", d.path("album").path("cover_xl").asText(null));
                    } else {
                        entry.put("preview", (String) null);
                    }
                } catch (Exception e) {
                    log.debug("Deezer mapping failed for playlist {} - {}: {}", artist, title, e.getMessage());
                    entry.put("preview", (String) null);
                }

                out.add(entry);
            }

            return out;
        } catch (IOException e) {
            log.error("IO error fetching Last.fm playlist page", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "upstream_io_error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "interrupted", e);
        }
    }
}
