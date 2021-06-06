package by.bsuir.audio_stream.client;

import by.bsuir.audio_stream.client.configuration.ClientConfiguration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.URL;
import java.util.MissingResourceException;

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
        stage.setTitle("Rubicon Music");
        try {
            Image icon = findIcon();
            stage.getIcons().add(icon);
        } catch (MissingResourceException e) {
            logger.error(e.getMessage());
        }
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.show();
    }

    private Image findIcon() throws MissingResourceException {
        String iconPath = ClientConfiguration.getInstance().getIconPath();
        InputStream iconInputStream = getClass().getResourceAsStream(iconPath);
        if (iconInputStream == null) {
            throw new MissingResourceException("Icon file was not found.", getClass().getSimpleName(), iconPath);
        }
        Image icon = new Image(iconInputStream);
        return icon;
    }
}
