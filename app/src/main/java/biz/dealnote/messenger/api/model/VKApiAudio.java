package biz.dealnote.messenger.api.model;

/**
 * An audio object describes an audio file and contains the following fields.
 */
public class VKApiAudio implements VKApiAttachment {

    /**
     * Audio ID.
     */
    public int id;

    /**
     * Audio owner ID.
     */
    public int owner_id;

    /**
     * Artist name.
     */
    public String artist;

    /**
     * Audio file title.
     */
    public String title;

    /**
     * Duration (in seconds).
     */
    public int duration;

    /**
     * Link to mp3.
     */
    public String url;

    /**
     * ID of the lyrics (if available) of the audio file.
     */
    public int lyrics_id;

    /**
     * ID of the album containing the audio file (if assigned).
     */
    public int album_id;

    /**
     * Genre ID. See the list of audio genres.
     */
    public int genre;

    /**
     * An access key using for get information about hidden objects.
     */
    public String access_key;

    /**
     * Creates empty Audio instance.
     */
    public VKApiAudio() {

    }

    @Override
    public String getType() {
        return TYPE_AUDIO;
    }

    /**
     * Audio object genres.
     */
    public final static class Genre {

        private Genre() {
        }

        public final static int ROCK = 1;
        public final static int POP = 2;
        public final static int RAP_AND_HIPHOP = 3;
        public final static int EASY_LISTENING = 4;
        public final static int DANCE_AND_HOUSE = 5;
        public final static int INSTRUMENTAL = 6;
        public final static int METAL = 7;
        public final static int DUBSTEP = 8;
        public final static int JAZZ_AND_BLUES = 9;
        public final static int DRUM_AND_BASS = 10;
        public final static int TRANCE = 11;
        public final static int CHANSON = 12;
        public final static int ETHNIC = 13;
        public final static int ACOUSTIC_AND_VOCAL = 14;
        public final static int REGGAE = 15;
        public final static int CLASSICAL = 16;
        public final static int INDIE_POP = 17;
        public final static int OTHER = 18;
        public final static int SPEECH = 19;
        public final static int ALTERNATIVE = 21;
        public final static int ELECTROPOP_AND_DISCO = 22;
    }
}