package by.bsuir.audio_stream.client.play;

public class AudioPlayerException extends RuntimeException {

    private static final long serialVersionUID = 6187468738096327881L;

    public AudioPlayerException() {
    }

    public AudioPlayerException(String message) {
        super(message);
    }

    public AudioPlayerException(String message, Throwable cause) {
        super(message, cause);
    }

    public AudioPlayerException(Throwable cause) {
        super(cause);
    }
}
