package by.bsuir.audio_stream.client.view;

import by.bsuir.audio_stream.common.AudioTrackDto;

public interface AudioStreamClientBuilder {

    Object buildAudioItem(AudioTrackDto audioTrackDto) throws NullPointerException;
}
