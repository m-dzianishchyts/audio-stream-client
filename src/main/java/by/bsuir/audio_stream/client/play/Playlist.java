package by.bsuir.audio_stream.client.play;

import by.bsuir.audio_stream.common.AudioTrackDto;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class Playlist {

    private final LinkedList<AudioTrackDto> originalTrackList;
    private LinkedList<AudioTrackDto> trackList;
    private ListIterator<AudioTrackDto> iterator;

    public Playlist(Collection<AudioTrackDto> trackCollection) {
        originalTrackList = new LinkedList<>(trackCollection);
        trackList = new LinkedList<>(originalTrackList);
        reset();
    }

    public AudioTrackDto getTrackByIndex(int index) {
        if (index < 0 || index >= originalTrackList.size()) {
            throw new IllegalArgumentException("Wrong index (" + index + "). Track list length: "
                                               + originalTrackList.size() + ".");
        }
        AudioTrackDto selectedTrack = originalTrackList.get(index);
        int indexOfSelectedTrackCurrentTrackList = trackList.indexOf(selectedTrack);
        iterator = trackList.listIterator(indexOfSelectedTrackCurrentTrackList);
        return selectedTrack;
    }

    public AudioTrackDto getNextTrack() throws NoSuchElementException {
        if (!iterator.hasNext()) {
            reset();
        }
        AudioTrackDto nextTrack = iterator.next();
        return nextTrack;
    }

    public AudioTrackDto getPreviousTrack() throws NoSuchElementException {
        if (!iterator.hasPrevious()) {
            reset();
        }
        AudioTrackDto previousTrack = iterator.previous();
        return previousTrack;
    }

    public AudioTrackDto getFirstTrack() throws NoSuchElementException {
        AudioTrackDto firstTrack = trackList.getFirst();
        return firstTrack;
    }

    public boolean isEmpty() {
        return trackList.isEmpty();
    }

    public final void reset() {
        iterator = trackList.listIterator();
    }

    public void setShuffle(boolean shuffleNeeded) {
        if (shuffleNeeded) {
            Collections.shuffle(trackList);
        } else {
            trackList = new LinkedList<>(originalTrackList);
            reset();
        }
    }
}
