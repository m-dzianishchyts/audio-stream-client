package by.bsuir.audio_stream.client.app;

import by.bsuir.audio_stream.client.util.UrlBuildingException;
import by.bsuir.audio_stream.client.util.UrlBuildingUtil;
import by.bsuir.audio_stream.common.AudioTrackDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class AudioStreamClientApp {

    private static final Logger logger = LogManager.getLogger(AudioStreamClientApp.class);

    private static AudioStreamClientApp instance;

    private AudioStreamClientApp() {
    }

    public static synchronized AudioStreamClientApp getInstance() {
        if (instance == null) {
            instance = new AudioStreamClientApp();
        }
        return instance;
    }

    public List<AudioTrackDto> searchForAudioTracks(String query) throws NullPointerException {
        URL searchUrl;
        try {
            searchUrl = UrlBuildingUtil.buildSearchUrl(query);
        } catch (UrlBuildingException e) {
            logger.error(e.getMessage());
            return Collections.emptyList();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HttpURLConnection httpURLConnection = (HttpURLConnection) searchUrl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            InputStream responseBodyStream = httpURLConnection.getInputStream();
            AudioTrackDto[] audioTrackDto = objectMapper.readValue(responseBodyStream, AudioTrackDto[].class);
            return List.of(audioTrackDto);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
