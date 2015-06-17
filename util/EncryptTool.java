package com.dxy.android.statistics.util;

import android.text.TextUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * brokge@gmail.com
 *
 *You needs add libary commons-codec-1.10.jar
 */

public class EncryptTool {
    /**
     * 加密内容
     *
     * @param content 明文
     * @param key     加密key
     * @param  alogorithm alogorithm
     * @return 密文
     */
    public static String encrypt(String content, String key,String alogorithm) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), alogorithm);
            Cipher cipher = Cipher.getInstance(alogorithm);// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] hexStr = cipher.doFinal(content.getBytes("utf-8"));
            return Base64Util.encodeBASE64(hexStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密内容
     *
     * @param keyBytes         key
     * @param encryptedContent 密文
     * @return 明文
     */
    public static String getDecryptedContent(byte[] keyBytes, String encryptedContent) {
        if (TextUtils.isEmpty(encryptedContent))
            return encryptedContent;
        byte contentBytes[] = Base64Util.decodeBASE64(encryptedContent);
        try {
            SecretKeySpec spec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, spec);
            cipher.getBlockSize();
            String res = new String(cipher.doFinal(contentBytes));
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedContent;
    }

}
