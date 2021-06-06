package by.bsuir.audio_stream.client.view.fxml;

import by.bsuir.audio_stream.client.util.TimeFormatUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class AudioItemBuilder {

    public Label buildLabel(String text, double width) {
        Label artistLabel = new Label(text);
        artistLabel.setPrefWidth(width);
        return artistLabel;
    }

    public StackPane buildPlayPauseArea(Button playButton, Button pauseButton) {
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(playButton, pauseButton);
        pauseButton.setVisible(false);
        return stackPane;
    }

    public Button buildPlayButton() {
        ImageView playIconImage = new ImageView();
        playIconImage.setFitHeight(16);
        playIconImage.setFitWidth(16);
        Button playButton = new Button(null, playIconImage);
        playButton.getStyleClass().addAll("flat-button", "play-button");
        playButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        playButton.setPrefSize(32, 32);
        return playButton;
    }

    public Button buildPauseButton() {
        ImageView playIconImage = new ImageView();
        playIconImage.setFitHeight(16);
        playIconImage.setFitWidth(16);
        Button playButton = new Button(null, playIconImage);
        playButton.getStyleClass().addAll("flat-button", "pause-button");
        playButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        playButton.setPrefSize(32, 32);
        return playButton;
    }

    public Label buildDuration(int seconds) {
        String minutesAndSeconds = TimeFormatUtils.convertSecondsToMinutesAndSeconds(seconds);
        Label durationLabel = new Label(minutesAndSeconds);
        durationLabel.setAlignment(Pos.CENTER_RIGHT);
        durationLabel.setMinWidth(Region.USE_PREF_SIZE);
        durationLabel.setMaxWidth(Double.MAX_VALUE);
        return durationLabel;
    }

    public ImageView buildCover() {
        ImageView cover = new ImageView();
        cover.setFitHeight(32);
        cover.setFitWidth(32);
        cover.getStyleClass().add("cover");
        return cover;
    }
}
