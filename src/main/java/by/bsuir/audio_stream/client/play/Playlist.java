package by.bsuir.audio_stream.client.play;

import by.bsuir.audio_stream.common.AudioTrackDto;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class Playlist {

    private final LinkedList<AudioTrackDto> originalTrackList;
    private LinkedList<AudioTrackDto> trackList;
    private int currentPosition;

    public Playlist(Collection<AudioTrackDto> trackCollection) {
        originalTrackList = new LinkedList<>(trackCollection);
        trackList = new LinkedList<>(originalTrackList);
    }

    public AudioTrackDto getTrackByIndex(int index) {
        if (index < 0 || index >= originalTrackList.size()) {
            throw new IllegalArgumentException("Wrong index (" + index + "). Track list length: "
                                               + originalTrackList.size() + ".");
        }
        AudioTrackDto selectedTrack = originalTrackList.get(index);
        currentPosition = trackList.indexOf(selectedTrack);
        return selectedTrack;
    }

    public AudioTrackDto getNextTrack() throws NoSuchElementException {
        if (currentPosition == trackList.size() - 1) {
            currentPosition = -1;
        }
        currentPosition++;
        AudioTrackDto nextTrack = trackList.get(currentPosition);
        return nextTrack;
    }

    public AudioTrackDto getPreviousTrack() throws NoSuchElementException {
        if (currentPosition == 0) {
            currentPosition = trackList.size();
        }
        currentPosition--;
        AudioTrackDto previousTrack = trackList.get(currentPosition);
        return previousTrack;
    }

    public AudioTrackDto getFirstTrack() throws NoSuchElementException {
        AudioTrackDto firstTrack = trackList.getFirst();
        return firstTrack;
    }

    public boolean isEmpty() {
        return trackList.isEmpty();
    }

    public void setShuffle(boolean shuffleNeeded) {
        if (shuffleNeeded) {
            Collections.shuffle(trackList);
        } else {
            AudioTrackDto currentTrack = trackList.get(currentPosition);
            trackList = new LinkedList<>(originalTrackList);
            currentPosition = trackList.indexOf(currentTrack);
        }
    }
}
