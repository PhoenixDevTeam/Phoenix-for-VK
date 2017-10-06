package biz.dealnote.messenger.crypt;

import com.scottyab.aescrypt.AESCrypt;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import biz.dealnote.messenger.crypt.ver.Version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by ruslan.kolbasa on 25.11.2016.
 * phoenix
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AESCrypt.class})
public class CryptHelperTest {

    private static final String AES128KEY = "Vb/tt9RRRymG+Oh9GsTyUg==";

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(AESCrypt.class);
    }

    @Test
    public void encryptWithAes() throws Exception {
        when(AESCrypt.encrypt(anyString(), anyString())).thenReturn("{encrypted_text}");

        String exapleText = "Hello, world!!!";

        String crypted = CryptHelper.encryptWithAes(exapleText, AES128KEY, "NULL", 15, KeyLocationPolicy.PERSIST);

        CryptHelper.EncryptedMessage message = CryptHelper.parseEncryptedMessage(crypted);
        assertNotNull(message);
        assertTrue(message.getSessionId() == 15);
        assertTrue(message.getKeyLocationPolicy() == KeyLocationPolicy.PERSIST);
        assertEquals(message.getOriginalBody(), "{encrypted_text}");
    }

    @Test
    public void encryptWithAesNegative() throws Exception {
        when(AESCrypt.encrypt(anyString(), anyString())).thenThrow(new GeneralSecurityException());

        String exapleText = "Hello, world!!!";

        String crypted = CryptHelper.encryptWithAes(exapleText, AES128KEY, "NULL", 15, KeyLocationPolicy.PERSIST);

        assertEquals("NULL", crypted);
    }

    @Test
    public void parseEncryptedMessage() throws Exception {
        String rightText = "AES173654:lskhufpergrtg";

        CryptHelper.EncryptedMessage message = CryptHelper.parseEncryptedMessage(rightText);
        assertNotNull(message);
        assertEquals(message.getOriginalBody(), "lskhufpergrtg");
        assertTrue(message.getKeyLocationPolicy() == KeyLocationPolicy.PERSIST);
        assertTrue(message.getSessionId() == 73654L);
    }

    @Test
    public void analizeMessageBody() throws Exception {
        Map<String, Integer> map = new HashMap<>();
        map.put("AES14234:insdhf8erg", MessageType.CRYPTED);
        map.put("AES24:ff", MessageType.CRYPTED);

        map.put("AES1:insdhf8erg", MessageType.NORMAL);
        map.put("AE14234insdhf8erg", MessageType.NORMAL);

        ExchangeMessage message = new ExchangeMessage.Builder(Version.CURRENT, 1, SessionState.INITIATOR_STATE_1)
                .setKeyLocationPolicy(KeyLocationPolicy.PERSIST)
                .create();

        map.put(message.toString(), MessageType.KEY_EXCHANGE);

        map.put(null, MessageType.NORMAL);
        map.put("", MessageType.NORMAL);
        map.put("aes1344:joffd", MessageType.NORMAL);

        for(Map.Entry<String, Integer> entry : map.entrySet()){
            //noinspection ResourceType
            assertTrue(CryptHelper.analizeMessageBody(entry.getKey()) == entry.getValue());
        }
    }
}