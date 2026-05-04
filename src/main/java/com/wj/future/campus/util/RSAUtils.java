package com.wj.future.campus.util;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtils {

    private static final String ALGORITHM = "RSA";

    /**
     * 公钥加密
     */
    public static String encrypt(String content, String publicKeyStr) throws Exception {
        // 将 Base64 字符串转回 PublicKey 对象
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(spec);

        // 初始化 Cipher
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] result = cipher.doFinal(content.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * 私钥解密
     */
    public static String decrypt(String base64Content, String privateKeyStr) throws Exception {
        // 将 Base64 字符串转回 PrivateKey 对象
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(spec);

        // 初始化 Cipher
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] contentBytes = Base64.getDecoder().decode(base64Content);
        byte[] result = cipher.doFinal(contentBytes);
        return new String(result, "UTF-8");
    }
}