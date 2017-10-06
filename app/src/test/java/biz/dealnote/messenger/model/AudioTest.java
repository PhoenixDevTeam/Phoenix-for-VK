package biz.dealnote.messenger.model;

import org.junit.Test;

import biz.dealnote.messenger.api.model.VKApiAudio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by admin on 04.12.2016.
 * phoenix
 */
public class AudioTest {

    @Test
    public void toDtoTest() throws Exception {
        Audio audio = new Audio()
                .setId(100)
                .setOwnerId(222)
                .setArtist("Ortem")
                .setTitle("Kolbasa")
                .setDuration(500)
                .setUrl("no-url")
                .setLyricsId(-400)
                .setAlbumId(-4)
                .setGenre(5)
                .setAccessKey("1234567890")
                .setDeleted(false);

        VKApiAudio dto = audio.toDto();
        assertTrue(audio.getId() == dto.id);
        assertTrue(audio.getOwnerId() == dto.owner_id);
        assertEquals(audio.getArtist(), dto.artist);
        assertEquals(audio.getTitle(), dto.title);
        assertTrue(audio.getDuration() == dto.duration);
        assertEquals(audio.getUrl(), dto.url);
        assertTrue(audio.getLyricsId() == dto.lyrics_id);
        assertTrue(audio.getAlbumId() == dto.album_id);
        assertTrue(audio.getGenre() == dto.genre);
        assertEquals(audio.getAccessKey(), dto.access_key);
        assertTrue(audio.isDeleted() == dto.deleted);
    }

}