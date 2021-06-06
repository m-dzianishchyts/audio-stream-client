package by.bsuir.audio_stream.client.view.fxml;

import by.bsuir.audio_stream.common.AudioTrackDto;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.Objects;

public abstract class AudioItem extends HBox {

    private final AudioTrackDto audioTrackDto;
    private final ObjectProperty<AudioItemState> itemState;
    private final Button playButton;
    private final Button pauseButton;

    protected AudioItem(AudioTrackDto audioTrackDto, ObjectProperty<AudioItemState> itemState)
            throws NullPointerException {
        this.itemState = itemState;
        Objects.requireNonNull(audioTrackDto);
        AudioItemBuilder audioItemBuilder = new AudioItemBuilder() {
        };
        playButton = audioItemBuilder.buildPlayButton();
        pauseButton = audioItemBuilder.buildPauseButton();
        this.audioTrackDto = audioTrackDto;
        itemState.addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case PAUSED, DEFAULT -> showPlayButton();
                case PLAYING -> showPauseButton();
            }
        });
    }

    public AudioItemState getItemState() {
        return itemState.get();
    }

    public ObjectProperty<AudioItemState> itemStateProperty() {
        return itemState;
    }

    public Button getPlayButton() {
        return playButton;
    }

    public Button getPauseButton() {
        return pauseButton;
    }

    public void showPlayButton() {
        playButton.setVisible(true);
        pauseButton.setVisible(false);
    }

    public void showPauseButton() {
        playButton.setVisible(false);
        pauseButton.setVisible(true);
    }

    public AudioTrackDto getAudioTrackDto() {
        return audioTrackDto;
    }

    public enum AudioItemState {
        PLAYING,
        PAUSED,
        DEFAULT
    }
}
