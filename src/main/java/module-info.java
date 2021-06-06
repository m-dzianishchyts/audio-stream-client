module by.bsuir.audio_stream.client {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    requires uk.co.caprica.vlcj;
    requires by.bsuir.audio_stream.common;
    requires com.fasterxml.jackson.databind;

    opens by.bsuir.audio_stream.client to javafx.graphics;
    opens by.bsuir.audio_stream.client.view.fxml to javafx.fxml;
}