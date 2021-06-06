package by.bsuir.audio_stream.client.play;

import by.bsuir.audio_stream.client.util.UrlBuildingException;
import by.bsuir.audio_stream.client.util.UrlBuildingUtil;
import by.bsuir.audio_stream.common.AudioTrackDto;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.caprica.vlcj.media.MediaStatistics;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;

public final class AudioStreamPlayer {

    private static final Logger logger = LogManager.getLogger(AudioStreamPlayer.class);

    private static AudioStreamPlayer instance;

    private final LongProperty currentTrackIdProperty;
    private final BooleanProperty loopedPlaying;
    private final BooleanProperty shufflePlaying;
    private final DoubleProperty loadedBuffer;
    private final DoubleProperty playedBuffer;
    private final LongProperty playedTime;
    private final BooleanProperty stopped;
    private final LongProperty duration;
    private final StringProperty title;
    private final StringProperty artist;

    private AudioTrackDto currentTrack;
    private Playlist playlist;

    private final AudioPlayerComponent playerComponent = new AudioPlayerComponent() {
        @Override
        public void playing(MediaPlayer mediaPlayer) {
            long currentTrackDuration = currentTrack.getDuration() * 1000L;
            Platform.runLater(() -> {
                title.set(currentTrack.getTitle());
                artist.set(currentTrack.getArtist());
                duration.set(currentTrackDuration);
            });
        }

        @Override
        public void finished(MediaPlayer mediaPlayer) {
            logger.info("Finished playing {} - {}.", currentTrack.getTitle(), currentTrack.getArtist());
            mediaPlayer.submit(AudioStreamPlayer.this::playNextTrackAuto);
        }

        @Override
        public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
            MediaStatistics mediaStatistics = mediaPlayer.media().info().statistics();
            double bytesLoaded = mediaStatistics.inputBytesRead();
            long currentTrackFileSize = currentTrack.getFileSize();
            long currentTrackDuration = currentTrack.getDuration() * 1000L;
            Platform.runLater(() -> {
                playedTime.set(newTime);
                loadedBuffer.set(bytesLoaded / currentTrackFileSize);
                playedBuffer.set((double) newTime / currentTrackDuration);
            });
            logger.trace("Bytes loaded: {}", bytesLoaded);
            logger.trace("Time played: {}", newTime);
        }


    };

    private AudioStreamPlayer() {
        playlist = new Playlist(Collections.emptyList());
        currentTrackIdProperty = new SimpleLongProperty();
        loopedPlaying = new SimpleBooleanProperty();
        shufflePlaying = new SimpleBooleanProperty();
        loadedBuffer = new SimpleDoubleProperty();
        playedBuffer = new SimpleDoubleProperty();
        playedTime = new SimpleLongProperty();
        stopped = new SimpleBooleanProperty();
        duration = new SimpleLongProperty();
        title = new SimpleStringProperty();
        artist = new SimpleStringProperty();
    }

    public static synchronized AudioStreamPlayer getInstance() {
        if (instance == null) {
            instance = new AudioStreamPlayer();
        }
        return instance;
    }

    public double getLoadedBuffer() {
        return loadedBuffer.get();
    }

    public DoubleProperty loadedBufferProperty() {
        return loadedBuffer;
    }

    public long getPlayedTime() {
        return playedTime.get();
    }

    public LongProperty playedTimeProperty() {
        return playedTime;
    }

    public long getDuration() {
        return duration.get();
    }

    public LongProperty durationProperty() {
        return duration;
    }

    public double getPlayedBuffer() {
        return playedBuffer.get();
    }

    public DoubleProperty playedBufferProperty() {
        return playedBuffer;
    }

    public void setPlaylistFromAudioTrackDtoList(Collection<AudioTrackDto> audioTrackDtoCollection) {
        if (audioTrackDtoCollection.isEmpty()) {
            throw new AudioPlayerException("Audio track DTO collection is empty.");
        }
        this.playlist = new Playlist(audioTrackDtoCollection);
        currentTrackIdProperty.setValue(null);
        updateCurrentTrack(playlist.getNextTrack());
        try {
            URL currentTrackUrl = buildUrlFromAudioTrackDto(currentTrack);
            playerComponent.mediaPlayer().controls().stop();
            playerComponent.mediaPlayer().media().start(currentTrackUrl.toString());
        } catch (UrlBuildingException e) {
            logger.error(e.getMessage());
        }
    }

    public void play() throws AudioPlayerException {
        playerComponent.mediaPlayer().controls().play();
    }

    public void pause() {
        playerComponent.mediaPlayer().controls().pause();
    }

    public void playNextTrack() {
        try {
            updateCurrentTrack(playlist.getNextTrack());
            URL currentTrackUrl = buildUrlFromAudioTrackDto(currentTrack);
            playerComponent.mediaPlayer().media().prepare(currentTrackUrl.toString());
            playerComponent.mediaPlayer().controls().play();
        } catch (NoSuchElementException e) {
            throw new AudioPlayerException("Playlist is empty.", e);
        } catch (UrlBuildingException e) {
            throw new AudioPlayerException(e);
        }
    }

    public void playTrackByIndex(int index) {
        updateCurrentTrack(playlist.getTrackByIndex(index));
        try {
            URL currentTrackUrl = buildUrlFromAudioTrackDto(currentTrack);
            playerComponent.mediaPlayer().media().prepare(currentTrackUrl.toString());
            playerComponent.mediaPlayer().controls().play();
        } catch (UrlBuildingException e) {
            logger.error(e.getMessage());
        }
    }

    public void seekProgress(double position) {
        if (currentTrack == null) {
            return;
        }
        if (position < 0 || position > 1) {
            throw new IllegalArgumentException("Position must be in range from 0 to 1.");
        }
        long msDuration = currentTrack.getDuration() * 1000L;
        long msTime = (int) (msDuration * position);
        playerComponent.mediaPlayer().controls().setTime(msTime);
    }

    public long getCurrentTrackIdProperty() {
        return currentTrackIdProperty.get();
    }

    public LongProperty currentTrackIdProperty() {
        return currentTrackIdProperty;
    }

    public void playPreviousTrack() {
        if (currentTrack.equals(playlist.getFirstTrack())) {
            playerComponent.mediaPlayer().controls().stop();
            playerComponent.mediaPlayer().controls().play();
            return;
        }
        try {
            updateCurrentTrack(playlist.getPreviousTrack());
            URL currentTrackUrl = buildUrlFromAudioTrackDto(currentTrack);
            playerComponent.mediaPlayer().media().prepare(currentTrackUrl.toString());
            if (loopedPlaying.get()) {
                playerComponent.mediaPlayer().controls().play();
            }
        } catch (NoSuchElementException e) {
            throw new AudioPlayerException("Playlist is empty.", e);
        } catch (UrlBuildingException e) {
            throw new AudioPlayerException(e);
        }
    }

    public void enableLoopedPlaying() {
        loopedPlaying.set(false);
    }

    public void disableLoopedPlaying() {
        loopedPlaying.set(true);
    }

    public void switchLoopedPlaying() {
        boolean oldValue = loopedPlaying.get();
        loopedPlaying.set(!oldValue);
        logger.info("Looped playing was switched to {}.", loopedPlaying.get());
    }

    public void enableShufflePlaying() {
        shufflePlaying.set(false);
    }

    public void disableShufflePlaying() {
        shufflePlaying.set(true);
    }

    public void switchShufflePlaying() {
        boolean oldValue = shufflePlaying.get();
        shufflePlaying.set(!oldValue);
        logger.info("Shuffle playing was switched to {}.", shufflePlaying.get());
    }

    public boolean getLoopedPlaying() {
        return loopedPlaying.get();
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getArtist() {
        return artist.get();
    }

    public StringProperty artistProperty() {
        return artist;
    }

    public BooleanProperty loopedPlayingProperty() {
        return loopedPlaying;
    }

    public boolean getShufflePlaying() {
        return shufflePlaying.get();
    }

    public BooleanProperty shufflePlayingProperty() {
        return shufflePlaying;
    }

    public void setShuffle(boolean shuffleNeeded) {
        playlist.setShuffle(shuffleNeeded);
    }

    public void setVolume(int volume) throws IllegalArgumentException {
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Volume must be between 0 and 100.");
        }
        playerComponent.mediaPlayer().audio().setVolume(volume);
    }

    public boolean getStopped() {
        return stopped.get();
    }

    public BooleanProperty stoppedProperty() {
        return stopped;
    }

    private URL buildUrlFromAudioTrackDto(AudioTrackDto audioTrackDto) throws UrlBuildingException {
        URL audioTrackUrl = UrlBuildingUtil.buildStreamUrl(audioTrackDto.getId());
        return audioTrackUrl;
    }

    private void updateCurrentTrack(AudioTrackDto newTrack) {
        currentTrack = newTrack;
        currentTrackIdProperty.set(currentTrack.getId());
    }

    private void playNextTrackAuto() {
        try {
            updateCurrentTrack(playlist.getNextTrack());
            URL currentTrackUrl = buildUrlFromAudioTrackDto(currentTrack);
            playerComponent.mediaPlayer().media().prepare(currentTrackUrl.toString());
            if (!currentTrack.equals(playlist.getFirstTrack()) || loopedPlaying.get()) {
                playerComponent.mediaPlayer().controls().play();
            } else {
                playerComponent.mediaPlayer().controls().stop();
                stopped.set(true);
            }
        } catch (NoSuchElementException e) {
            throw new AudioPlayerException("Playlist is empty.", e);
        } catch (UrlBuildingException e) {
            throw new AudioPlayerException(e);
        }
    }
}
