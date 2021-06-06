package by.bsuir.audio_stream.client.util;

import by.bsuir.audio_stream.client.configuration.ClientConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class UrlBuildingUtil {

    private UrlBuildingUtil() {
    }

    public static URL buildSearchUrl(String query) throws UrlBuildingException {
        Objects.requireNonNull(query);
        ClientConfiguration clientConfiguration = ClientConfiguration.getInstance();
        URL serverUrl = clientConfiguration.getServerUrl();
        try {
            String validQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            URL searchUrl = new URL(serverUrl, "audio/search?query=" + validQuery);
            return searchUrl;
        } catch (MalformedURLException e) {
            throw new UrlBuildingException("Search URL is malformed.", e);
        }
    }

    public static URL buildStreamUrl(long audioTrackId) throws UrlBuildingException {
        ClientConfiguration clientConfiguration = ClientConfiguration.getInstance();
        URL serverUrl = clientConfiguration.getServerUrl();
        try {
            URL searchUrl = new URL(serverUrl, "audio/stream?id=" + audioTrackId);
            return searchUrl;
        } catch (MalformedURLException e) {
            throw new UrlBuildingException("Search URL is malformed.", e);
        }
    }
}
