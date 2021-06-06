package by.bsuir.audio_stream.client.view.fxml;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AudioItemListUpdateListener implements Runnable {

    private static final Logger logger = LogManager.getLogger(AudioItemListUpdateListener.class);

    private final Pane container;
    private final List<AudioItem> audioItems;

    public AudioItemListUpdateListener(Pane container, List<AudioItem> audioItems) {
        this.container = container;
        this.audioItems = audioItems;
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (audioItems) {
                    audioItems.wait();
                    logger.debug("Updating audio item container.");
                    Platform.runLater(() -> {
                        container.getChildren().clear();
                        container.getChildren().addAll(audioItems);
                    });
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
