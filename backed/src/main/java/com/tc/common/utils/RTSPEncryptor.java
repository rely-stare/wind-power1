package com.tc.common.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class RTSPEncryptor {

    private static final String AES_KEY = "AyYkmAVZ6XsUes0ICjq5TA=="; // 16字节密钥

    // 加密方法
    public static String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // 解密方法
    public static String decrypt(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        return new String(cipher.doFinal(decoded));
    }

    public static String generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }



    public static void main(String[] args) throws Exception {

//        String rtspUrl = "rtsp://example.com/stream";
//        String encryptedUrl = encrypt(rtspUrl);
//        System.out.println("Encrypted URL: " + encryptedUrl);

        String encryptedUrl = "7RPcWkmIn8nQWEI6l7MaGZooDwKCj/dYTDgbT99AUgswJPUzHsZ07H9JluYRlT/1LC01Z8gH+V+HeS3meEYkvj/uRgoyPKOUl+4aUJohFa8rQe58O1Jq91BIzgQZPm9PE9twijMp9KmNB4YhZ+KlW64QX9vgfY9TOoyXw/BIvV4=";

        String decryptUrl = decrypt(encryptedUrl);
        System.out.println("Decrypted URL: " + decryptUrl);

    }


}
