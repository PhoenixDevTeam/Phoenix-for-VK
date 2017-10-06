package biz.dealnote.messenger.player;

import biz.dealnote.messenger.model.Audio;
import java.util.List;

interface IAudioPlayerService {
    void openFile(in Audio audio);
    void open(in List<Audio> list, int position);
    void stop();
    void pause();
    void play();
    void prev();
    void next();
    void setShuffleMode(int shufflemode);
    void setRepeatMode(int repeatmode);
    void refresh();
    boolean isPlaying();
    boolean isPreparing();
    boolean isInitialized();
    List<Audio> getQueue();
    long duration();
    long position();
    long seek(long pos);
    Audio getCurrentAudio();
    String getArtistName();
    String getTrackName();
    String getAlbumName();
    String getPath();
    int getQueuePosition();
    int getShuffleMode();
    int getRepeatMode();
    int getAudioSessionId();
    int getBufferPercent();
}

