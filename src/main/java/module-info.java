module by.bsuir {
    requires java.desktop;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;

    exports by.bsuir.audio_stream.client.view;
    exports by.bsuir.audio_stream.client;

    opens by.bsuir.audio_stream.client to javafx.fxml;
    opens by.bsuir.audio_stream.client.view to javafx.fxml;
}