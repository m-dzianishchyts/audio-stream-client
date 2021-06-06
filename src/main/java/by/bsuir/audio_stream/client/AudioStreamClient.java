package by.bsuir.audio_stream.client;

import by.bsuir.audio_stream.client.configuration.ClientConfiguration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

public class AudioStreamClient extends Application {

    private static final Logger logger = LogManager.getLogger(AudioStreamClient.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        logger.info("Starting application.");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        String fxmlDirectoryPath = ClientConfiguration.getInstance().getProperties().getProperty("fxml.path");
        if (fxmlDirectoryPath == null) {
            logger.error("Failed to locate FXML directory (property not found).");
            System.exit(1);
        }
        URL fxmlUrl = getClass().getResource(fxmlDirectoryPath + "view.fxml");
        if (fxmlUrl == null) {
            logger.error("Failed to load FXML file (file not found).");
            System.exit(3);
        }
        Parent root = FXMLLoader.load(fxmlUrl);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.show();
    }
}
