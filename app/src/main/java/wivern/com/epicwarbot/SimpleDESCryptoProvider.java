package wivern.com.epicwarbot;

import android.util.Base64;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * simple des crypto provider for decrypt/encrypt text.
 */
public final class SimpleDESCryptoProvider {
    /**
     * unicode format.
     */
    private static final String UNICODE_FORMAT = "UTF8";
    /**
     * encryption scheme.
     */
    public static final String DES_ENCRYPTION_SCHEME = "DES";
    /**
     * cipher.
     */
    private Cipher cipher;
    /**
     * secret key.
     */
    private SecretKey key;
    /**
     * default constructor.
     * @throws Exception exception
     */
    public SimpleDESCryptoProvider() throws Exception {
        KeySpec myKeySpec;
        SecretKeyFactory mySecretKeyFactory;
        byte[] keyAsBytes;
        String myEncryptionKey;
        String myEncryptionScheme;
        myEncryptionKey = "ThisIsSecretEncreptionKey";
        myEncryptionScheme = DES_ENCRYPTION_SCHEME;
        keyAsBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
        myKeySpec = new DESKeySpec(keyAsBytes);
        mySecretKeyFactory = SecretKeyFactory.getInstance(myEncryptionScheme);
        cipher = Cipher.getInstance(myEncryptionScheme);
        key = mySecretKeyFactory.generateSecret(myKeySpec);
    }

    /**
     * Method To Encrypt The String.
     * @param unencryptedString unencrypted string
     * @return encrypted string
     */
    public String encrypt(final String unencryptedString) {
        String encryptedString = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
            byte[] encryptedText = cipher.doFinal(plainText);
            encryptedString = Base64.encodeToString(encryptedText,
                    Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedString;
    }

    /**
     * Method To Decrypt An encrypted String.
     * @param encryptedString encrypted string
     * @return decrypted string
     */
    public String decrypt(final String encryptedString) {
        String decryptedText = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedText = Base64.decode(encryptedString,
                    Base64.DEFAULT);
            byte[] plainText = cipher.doFinal(encryptedText);
            decryptedText = bytes2String(plainText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedText;
    }

    /**
     * Returns String From An Array Of Bytes.
     * @param bytes bytes
     * @return string
     */
    private static String bytes2String(final byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        for (byte bfI: bytes) {
            stringBuffer.append((char) bfI);
        }
        return stringBuffer.toString();
    }
}
