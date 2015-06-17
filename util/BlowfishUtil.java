package com.dxy.android.statistics.util;

import com.dxy.android.statistics.DXYStatisticsConfig;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

/**
 * Created with IntelliJ IDEA.
 * User: brokge@gmail.com
 * Date: 14/11/18
 * Time: 11:42
 */
public class BlowfishUtil {
    //private static Logger syslogger = Logger.getLogger("sys");
    private final static String ALGORITM = "Blowfish";
    private final static String KEY = DXYStatisticsConfig.LOGENCRYPT;
    private static Cipher ecipher;
    private static Cipher dcipher;

    static {
        try {
            ecipher = Cipher.getInstance(ALGORITM);
            ecipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY.getBytes(), ALGORITM));

            dcipher = Cipher.getInstance(ALGORITM);
            dcipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY.getBytes(), ALGORITM));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(encrypt("123456"));
    }


    public static String encrypt(String plainText) {
        try {
            return HexUtil.bytesToHex(ecipher.doFinal(plainText.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            // syslogger.error(e);

        }
        return "";
    }

    public static String decrypt(String encryptedText) {
        try {
            byte[] decrypted = dcipher.doFinal(HexUtil.hexToBytes(encryptedText));
            return new String(decrypted);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            //syslogger.error(e);
        }
        return "";
    }

}
