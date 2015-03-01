package wivern.com.epicwarbot;

import android.util.Log;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * simple des crypto provider for decrypt/encrypt text.
 */
public final class SimpleDESCryptoProvider {
    /**
     * tag for log.
     */
    private static final String LOG_TAG = "SimpleDESCryptoProvider";
    /**
     * hex.
     */
    private static final String HEX = "0123456789ABCDEF";
    /**
     * private constructor.
     */
    private SimpleDESCryptoProvider() {
        //not called
    }

    /**
     * seed.
     */
    public static final String SEED = "AM6R0FFBABFAKIILEMALL";

    /**
     * decrypt text.
     *
     * @param src encrypted text
     * @return decrypted text
     */
    public static String decrypt(final String src) {

        try {
            javax.crypto.spec.SecretKeySpec key
                    = new javax.crypto.spec.SecretKeySpec(getRawKey(), "DES");
            Cipher ecipher = Cipher.getInstance("DES");
            ecipher.init(Cipher.DECRYPT_MODE, key);

            byte[] utf8 = toByte(src);

            // Descrypt
            byte[] dec = ecipher.doFinal(utf8);

            return new String(dec);
        } catch (Exception exc) {
            try {
                exc.printStackTrace();
            } catch (Exception exc2) {
                Log.d(LOG_TAG, exc2.toString());
            }

        }
        return src;

    }

    /**
     * encrypt text.
     *
     * @param src decrypted text
     * @return encrypted text
     */
    public static String encrypt(final String src) {
        try {
            javax.crypto.spec.SecretKeySpec
                    key = new javax.crypto.spec.SecretKeySpec(getRawKey(),
                    "DES");
            Cipher ecipher = Cipher.getInstance("DES");
            ecipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] utf8 = src.getBytes("UTF8");

            // Encrypt
            byte[] enc = ecipher.doFinal(utf8);

            return toHex(enc);
        } catch (Exception exc) {
            try {
                exc.printStackTrace();
            } catch (Exception exc2) {
                Log.d(LOG_TAG, exc2.toString());
            }

        }
        return src;
    }

    /**
     * get raw key.
     * @return raw key
     * @throws Exception exception
     */
    private static byte[] getRawKey() throws Exception {
        final int iM = 56;
        KeyGenerator kgen = KeyGenerator.getInstance("DES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(SEED.getBytes());
        kgen.init(iM, sr);
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

    /**
     * text to hex.
     * @param txt text
     * @return hex
     */
    public static String toHex(final String txt) {
        return toHex(txt.getBytes());
    }

    /**
     * text from hex.
     * @param hex hex
     * @return text
     */
    public static String fromHex(final String hex) {
        return new String(toByte(hex));
    }

    /**
     * hex string to byte array.
     * @param hexString hex string
     * @return byte array
     */
    public static byte[] toByte(final String hexString) {
        final int dec = 16;
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    dec).byteValue();
        }
        return result;
    }

    /**
     * byte array to hex string.
     * @param buf byte array
     * @return hex string
     */
    public static String toHex(final byte[] buf) {
        if (buf == null) {
            return "";
        }
        StringBuffer result = new StringBuffer(2 * buf.length);
//        for (int i = 0; i < buf.length; i++) {
//            appendHex(result, buf[i]);
//        }
        for (byte bf : buf) {
            appendHex(result, bf);
        }
        return result.toString();
    }

    /**
     * append hex.
     * @param sb string buffer
     * @param b byte
     */
    private static void appendHex(final StringBuffer sb, final byte b) {
        final int moveB = 4;
        final int hexF = 0x0f;
        sb.append(HEX.charAt((b >> moveB) & hexF)).append(HEX.charAt(b & hexF));
    }
}
