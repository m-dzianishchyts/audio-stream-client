package by.bsuir.audio_stream.client.app;

import java.io.Serial;

public class AudioStreamClientAppException extends Exception {

    @Serial
    private static final long serialVersionUID = -6626851272828811274L;

    public AudioStreamClientAppException() {
    }

    public AudioStreamClientAppException(String message) {
        super(message);
    }

    public AudioStreamClientAppException(String message, Throwable cause) {
        super(message, cause);
    }

    public AudioStreamClientAppException(Throwable cause) {
        super(cause);
    }
}
