package by.bsuir.audio_stream.client.view.fxml;

import by.bsuir.audio_stream.client.app.AudioStreamClientApp;
import by.bsuir.audio_stream.client.configuration.ClientConfiguration;
import by.bsuir.audio_stream.client.play.AudioStreamPlayer;
import by.bsuir.audio_stream.client.util.TimeFormatUtils;
import by.bsuir.audio_stream.common.AudioTrackDto;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FxClientController implements Initializable {

    private static final Logger logger = LogManager.getLogger(FxClientController.class);
    private static final String ACTIVE_CLASS = "active";
    private final List<AudioItem> searchAudioItems;
    private final List<AudioItem> playlistAudioItems;
    private final Supplier<Timer> searchTimerSupplier = () -> new Timer("search-timer");
    @FXML
    private BorderPane parent;
    @FXML
    private StackPane playPauseContainer;
    @FXML
    private Button playButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button shuffleButton;
    @FXML
    private Button loopButton;
    @FXML
    private TextField searchField;
    @FXML
    private VBox searchResultContainer;
    @FXML
    private VBox playlistContainer;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ProgressBar bufferBar;
    @FXML
    private Label playedTime;
    @FXML
    private Label duration;
    @FXML
    private Label title;
    @FXML
    private Label artist;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ImageView volumeIcon;

    private AudioItem currentTrackAudioItem;
    private Timer searchTimer = searchTimerSupplier.get();

    public FxClientController() {
        searchAudioItems = new ArrayList<>();
        playlistAudioItems = new ArrayList<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchResultContainer.getChildren().clear();
        playlistContainer.getChildren().clear();

        searchField.setOnKeyTyped(event -> {
            searchTimer.cancel();
            searchResultContainer.getChildren().clear();
            TimerTask timerTask = new DelayedSearchTask();
            searchTimer = searchTimerSupplier.get();
            searchTimer.schedule(timerTask, 1000);
        });

        launchAudioItemListListenerThread("search list listener", searchResultContainer, searchAudioItems);
        launchAudioItemListListenerThread("playlist listener", playlistContainer, playlistAudioItems);

        AudioStreamPlayer audioStreamPlayer = AudioStreamPlayer.getInstance();

        audioStreamPlayer.currentTrackIdProperty().addListener((observable, oldValue, newValue) -> {
            playlistAudioItems
                    .forEach(audioItem -> audioItem.itemStateProperty().set(AudioItem.AudioItemState.DEFAULT));
            searchAudioItems.forEach(audioItem -> audioItem.itemStateProperty().set(AudioItem.AudioItemState.DEFAULT));
            playlistAudioItems.stream()
                              .filter(audioItem -> newValue != null
                                                   && audioItem.getAudioTrackDto().getId() == newValue.longValue())
                              .forEach(
                                      audioItem -> audioItem.itemStateProperty().set(AudioItem.AudioItemState.PLAYING));
            searchAudioItems.stream()
                            .filter(audioItem -> newValue != null
                                                 && audioItem.getAudioTrackDto().getId() == newValue.longValue())
                            .forEach(audioItem -> audioItem.itemStateProperty().set(AudioItem.AudioItemState.PLAYING));
        });

        audioStreamPlayer.shufflePlayingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue) {
                shuffleButton.getStyleClass().add(ACTIVE_CLASS);
            } else {
                shuffleButton.getStyleClass().remove(ACTIVE_CLASS);
            }
        });
        audioStreamPlayer.loopedPlayingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue) {
                loopButton.getStyleClass().add(ACTIVE_CLASS);
            } else {
                loopButton.getStyleClass().remove(ACTIVE_CLASS);
            }
        });
        audioStreamPlayer.playedTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String minutesAndSeconds = TimeFormatUtils.convertMillisecondsToMinutesAndSeconds(newValue.longValue());
                playedTime.setText(minutesAndSeconds);
            }
        });
        audioStreamPlayer.durationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String minutesAndSeconds = TimeFormatUtils.convertMillisecondsToMinutesAndSeconds(newValue.longValue());
                duration.setText(minutesAndSeconds);
            }
        });
        audioStreamPlayer.stoppedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue) {
                setStateForAll(playlistAudioItems, AudioItem.AudioItemState.DEFAULT);
                setStateForAll(searchAudioItems, AudioItem.AudioItemState.DEFAULT);
                showPlayButton();
                audioStreamPlayer.stoppedProperty().set(false);
            }
        });
        title.textProperty().bind(audioStreamPlayer.titleProperty());
        artist.textProperty().bind(audioStreamPlayer.artistProperty());
        progressBar.progressProperty().bind(AudioStreamPlayer.getInstance().playedBufferProperty());
        bufferBar.progressProperty().bind(AudioStreamPlayer.getInstance().loadedBufferProperty());

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            int volume = newValue.intValue();
            ClientConfiguration clientConfiguration = ClientConfiguration.getInstance();
            Path fxmlDirectoryPath = clientConfiguration.getFxmlDirectoryPath();
            Path volumeIconPath;
            if (volume == 0) {
                volumeIconPath = fxmlDirectoryPath.resolve("icons/volume-muted_32px.png");
            } else if (volume < 33) {
                volumeIconPath = fxmlDirectoryPath.resolve("icons/volume-low_32px.png");
            } else if (volume < 66) {
                volumeIconPath = fxmlDirectoryPath.resolve("icons/volume-moderate_32px.png");
            } else {
                volumeIconPath = fxmlDirectoryPath.resolve("icons/volume-high_32px.png");
            }
            InputStream iconStream = getClass().getResourceAsStream(volumeIconPath.toString().replace("\\", "/"));
            if (iconStream == null) {
                logger.error("Volume icon was not found.");
                return;
            }
            volumeIcon.setImage(new Image(iconStream));
            AudioStreamPlayer.getInstance().setVolume(volume);
        });
        showPlayButton();
    }

    @FXML
    void handleProgressBarPressed() {
        if (playlistAudioItems.isEmpty()) {
            return;
        }
        logger.debug("Progress bar pressed.");
        progressBar.progressProperty().unbind();
    }

    @FXML
    void handleProgressBarDragged(MouseEvent event) {
        if (playlistAudioItems.isEmpty()) {
            return;
        }
        logger.debug("Progress bar dragged.");
        progressBar.progressProperty().unbind();
        double selectedProgress = calculateRelativeProgress(event);
        progressBar.progressProperty().set(selectedProgress);
    }

    @FXML
    void handleProgressBarReleased(MouseEvent event) {
        if (playlistAudioItems.isEmpty()) {
            return;
        }
        double selectedProgress = calculateRelativeProgress(event);
        logger.debug("Progress bar released. Selected progress: {}.", selectedProgress);
        AudioStreamPlayer.getInstance().seekProgress(selectedProgress);
        TimerTask delayedProgressBarBind = new TimerTask() {
            @Override
            public void run() {
                progressBar.progressProperty().bind(AudioStreamPlayer.getInstance().playedBufferProperty());
            }
        };
        Timer timer = new Timer();
        timer.schedule(delayedProgressBarBind, 500);
    }

    @FXML
    void handlePlayButton() {
        if (playlistAudioItems.isEmpty()) {
            return;
        }
        setStateForAliases(searchAudioItems, currentTrackAudioItem, AudioItem.AudioItemState.PLAYING);
        setStateForAliases(playlistAudioItems, currentTrackAudioItem, AudioItem.AudioItemState.PLAYING);
        showPauseButton();
        AudioStreamPlayer audioStreamPlayer = AudioStreamPlayer.getInstance();
        audioStreamPlayer.play();
    }

    @FXML
    void handlePauseButton() {
        if (playlistAudioItems.isEmpty()) {
            return;
        }
        setStateForAliases(searchAudioItems, currentTrackAudioItem, AudioItem.AudioItemState.PAUSED);
        setStateForAliases(playlistAudioItems, currentTrackAudioItem, AudioItem.AudioItemState.PAUSED);
        showPlayButton();
        AudioStreamPlayer audioStreamPlayer = AudioStreamPlayer.getInstance();
        audioStreamPlayer.pause();
    }

    @FXML
    void handleNextTrackButton() {
        if (playlistAudioItems.isEmpty()) {
            return;
        }
        AudioStreamPlayer audioStreamPlayer = AudioStreamPlayer.getInstance();
        audioStreamPlayer.playNextTrack();
    }

    @FXML
    void handlePreviousTrackButton() {
        if (playlistAudioItems.isEmpty()) {
            return;
        }
        AudioStreamPlayer audioStreamPlayer = AudioStreamPlayer.getInstance();
        audioStreamPlayer.playPreviousTrack();
    }

    @FXML
    void handleLoopButton() {
        AudioStreamPlayer audioStreamPlayer = AudioStreamPlayer.getInstance();
        audioStreamPlayer.switchLoopedPlaying();
    }

    @FXML
    void handleShuffleButton() {
        AudioStreamPlayer audioStreamPlayer = AudioStreamPlayer.getInstance();
        audioStreamPlayer.switchShufflePlaying();
    }

    private double calculateRelativeProgress(MouseEvent event) {
        double pixelProgress = event.getX();
        double selectedProgress;
        if (pixelProgress < 0) {
            selectedProgress = 0;
        } else if (pixelProgress > progressBar.getWidth()) {
            selectedProgress = 1;
        } else {
            selectedProgress = pixelProgress / progressBar.getWidth();
        }
        return selectedProgress;
    }

    private void launchAudioItemListListenerThread(String threadName, Pane container, List<AudioItem> audioItems) {
        Objects.requireNonNull(threadName);
        Objects.requireNonNull(container);
        Objects.requireNonNull(audioItems);
        Thread searchListListener = new Thread(new AudioItemListUpdateListener(container, audioItems));
        searchListListener.setName(threadName);
        searchListListener.setDaemon(true);
        searchListListener.start();
    }

    private void showPlayButton() {
        playButton.setVisible(true);
        pauseButton.setVisible(false);
    }

    private void showPauseButton() {
        playButton.setVisible(false);
        pauseButton.setVisible(true);
    }

    private void setStateForAliases(List<AudioItem> audioItems, AudioItem sample,
                                    AudioItem.AudioItemState audioItemState) {
        audioItems.stream()
                  .filter(audioItem -> audioItem.getAudioTrackDto().getId() == sample.getAudioTrackDto().getId())
                  .forEach(audioItem -> audioItem.itemStateProperty().set(audioItemState));
    }

    private void setStateForAll(List<AudioItem> audioItems, AudioItem.AudioItemState audioItemState) {
        audioItems.forEach(audioItem -> audioItem.itemStateProperty().set(audioItemState));
    }

    public class SearchAudioItem extends AudioItem {

        private static final Logger logger = LogManager.getLogger(SearchAudioItem.class);

        public SearchAudioItem(AudioTrackDto audioTrackDto) {
            super(audioTrackDto, new SimpleObjectProperty<>(AudioItemState.DEFAULT));
            getStyleClass().add("search-result-track");
            setAlignment(Pos.CENTER_LEFT);
            setPadding(new Insets(0, 30, 0, 10));

            AudioItemBuilder searchAudioClientBuilder = new AudioItemBuilder();
            StackPane playPauseArea = searchAudioClientBuilder.buildPlayPauseArea(getPlayButton(), getPauseButton());
            ImageView cover = searchAudioClientBuilder.buildCover();
            Label titleLabel = searchAudioClientBuilder.buildLabel(audioTrackDto.getTitle(), 300);
            Label artistLabel = searchAudioClientBuilder.buildLabel(audioTrackDto.getArtist(), 300);
            Label durationLabel = searchAudioClientBuilder.buildDuration(audioTrackDto.getDuration());

            getChildren().addAll(playPauseArea, cover, titleLabel, artistLabel, durationLabel);
            HBox.setMargin(cover, new Insets(5, 15, 5, 15));
            HBox.setHgrow(durationLabel, Priority.ALWAYS);
        }

        public class AudioItemClickHandler implements EventHandler<MouseEvent> {

            private final AudioItem target;
            private final List<AudioItem> searchAudioItems;
            private final List<AudioItem> playlistAudioItems;

            public AudioItemClickHandler(AudioItem target,
                                         List<AudioItem> searchAudioItems,
                                         List<AudioItem> playlistAudioItems) {
                this.target = Objects.requireNonNull(target);
                target.getPauseButton().setOnMouseClicked(this);
                target.getPlayButton().setOnMouseClicked(this);
                this.searchAudioItems = searchAudioItems;
                this.playlistAudioItems = playlistAudioItems;
            }

            @Override
            public void handle(MouseEvent event) {
                logger.debug("Clicked on AudioItem with id {}.", target);
                AudioItemState itemState = target.getItemState();
                switch (itemState) {
                    case PLAYING -> {
                        setStateForAliases(playlistAudioItems, target, AudioItemState.PAUSED);
                        target.itemStateProperty().set(AudioItemState.PAUSED);
                        handlePauseButton();
                    }
                    case PAUSED -> {
                        setStateForAliases(playlistAudioItems, target, AudioItemState.PLAYING);
                        target.itemStateProperty().set(AudioItemState.PLAYING);
                        handlePlayButton();
                    }
                    case DEFAULT -> {
                        currentTrackAudioItem = target;
                        List<PlayListAudioItem> audioItems = searchAudioItems
                                .stream()
                                .map(audioItem -> new PlayListAudioItem(audioItem.getAudioTrackDto()))
                                .collect(Collectors.toList());
                        synchronized (playlistAudioItems) {
                            playlistAudioItems.clear();
                            playlistAudioItems.addAll(audioItems);
                            int indexOfTarget = searchAudioItems.indexOf(target);
                            Collections.rotate(playlistAudioItems, -indexOfTarget);
                            audioItems.forEach(audioItem -> {
                                var audioItemClickHandler = audioItem.new AudioItemClickHandler(audioItem,
                                                                                                playlistAudioItems);
                                audioItem.setOnMouseClicked(audioItemClickHandler);
                            });
                            playlistAudioItems.notifyAll();
                        }
                        synchronized (playlistAudioItems) {
                            List<AudioTrackDto> audioTrackDtoList = playlistAudioItems
                                    .stream()
                                    .map(AudioItem::getAudioTrackDto).collect(Collectors.toList());
                            AudioStreamPlayer.getInstance().setPlaylistFromAudioTrackDtoList(audioTrackDtoList);
                        }
                        target.itemStateProperty().set(AudioItemState.PLAYING);
                        FxClientController.this.showPauseButton();
                    }
                }
            }
        }
    }

    private class DelayedSearchTask extends TimerTask {

        @Override
        public void run() {
            String query = searchField.getText();
            if (query == null || query.isBlank()) {
                return;
            }
            logger.info("Searching for {}...", searchField.getText());
            List<AudioTrackDto> audioTrackDtoList = AudioStreamClientApp.getInstance().searchForAudioTracks(query);
            synchronized (searchAudioItems) {
                List<SearchAudioItem> audioItems = audioTrackDtoList.stream()
                                                                    .map(SearchAudioItem::new)
                                                                    .collect(Collectors.toList());
                searchAudioItems.clear();
                searchAudioItems.addAll(audioItems);
                audioItems.forEach(audioItem -> {
                    var audioItemClickHandler = audioItem.new AudioItemClickHandler(audioItem,
                                                                                    searchAudioItems,
                                                                                    playlistAudioItems);
                    audioItem.setOnMouseClicked(audioItemClickHandler);
                });
                searchAudioItems.notifyAll();
            }
        }
    }

    private class PlayListAudioItem extends AudioItem {

        private static final Logger logger = LogManager.getLogger(PlayListAudioItem.class);

        public PlayListAudioItem(AudioTrackDto audioTrackDto) throws NullPointerException {
            super(audioTrackDto, new SimpleObjectProperty<>(AudioItemState.DEFAULT));
            setAlignment(Pos.CENTER_LEFT);
            setPadding(new Insets(5, 20, 5, 5));
            setSpacing(10);
            getStyleClass().addAll("playlist-track");
            AudioItemBuilder audioItemBuilder = new AudioItemBuilder();
            StackPane playPauseArea = audioItemBuilder.buildPlayPauseArea(getPlayButton(), getPauseButton());
            Label titleLabel = audioItemBuilder.buildLabel(audioTrackDto.getTitle(), 300);
            Label artistLabel = audioItemBuilder.buildLabel(audioTrackDto.getArtist(), 300);
            Label durationLabel = audioItemBuilder.buildDuration(audioTrackDto.getDuration());
            getChildren().addAll(playPauseArea, titleLabel, artistLabel, durationLabel);
        }

        public class AudioItemClickHandler implements EventHandler<MouseEvent> {

            final int indexOfAudioItemInPlaylist;
            private final AudioItem target;

            public AudioItemClickHandler(AudioItem target, List<AudioItem> playlistAudioItems) {
                this.target = Objects.requireNonNull(target);
                target.getPauseButton().setOnMouseClicked(this);
                target.getPlayButton().setOnMouseClicked(this);
                indexOfAudioItemInPlaylist = playlistAudioItems.indexOf(this.target);
            }

            @Override
            public void handle(MouseEvent event) {
                logger.debug("Clicked on item on playlist with id {}.", target);
                AudioItemState itemState = target.getItemState();
                switch (itemState) {
                    case PLAYING -> {
                        setStateForAliases(searchAudioItems, target, AudioItemState.PAUSED);
                        target.itemStateProperty().set(AudioItemState.PAUSED);
                        handlePauseButton();
                    }
                    case PAUSED -> {
                        setStateForAliases(searchAudioItems, target, AudioItemState.PLAYING);
                        target.itemStateProperty().set(AudioItemState.PLAYING);
                        handlePlayButton();
                    }
                    case DEFAULT -> {
                        currentTrackAudioItem = target;
                        setStateForAll(playlistAudioItems, AudioItemState.DEFAULT);
                        setStateForAliases(searchAudioItems, target, AudioItemState.PLAYING);
                        target.itemStateProperty().set(AudioItemState.PLAYING);
                        AudioStreamPlayer.getInstance().playTrackByIndex(indexOfAudioItemInPlaylist);
                        FxClientController.this.showPauseButton();
                    }
                }
            }
        }
    }
}
