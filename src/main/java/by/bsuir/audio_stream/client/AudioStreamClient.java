package by.bsuir.audio_stream.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class AudioStreamClient extends Application {
    
    public static Stage stage = null;
    private static final String FXML_NAME = "veiw.fxml";
    
    @Override
    public void start(Stage stage) throws Exception {
        URL fxmlUrl = getClass().getResource("view/" + FXML_NAME);
        if (fxmlUrl == null) {
            throw new IllegalStateException(FXML_NAME + " was not found.");
        }
        Parent root = FXMLLoader.load(fxmlUrl);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        AudioStreamClient.stage = stage;
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
