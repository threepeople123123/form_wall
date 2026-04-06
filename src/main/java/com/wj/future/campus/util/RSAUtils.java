package com.wj.future.campus.util;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtils {

    private static final String ALGORITHM = "RSA";


    public static void main(String[] args) {
        try {
            // 1. 初始化密钥对生成器，指定算法为 RSA
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALGORITHM);

            // 2. 初始化密钥长度 (通常为 2048 位)
            keyPairGen.initialize(2048);

            // 3. 生成密钥对
            KeyPair keyPair = keyPairGen.generateKeyPair();

            // 4. 获取私钥和公钥
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // 5. 转换为 Base64 编码的字符串（PKCS#8 格式）
            String privateKeyEncoded = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            String publicKeyEncoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            System.out.println("-----PRIVATE KEY (PKCS#8)-----");
            System.out.println(privateKeyEncoded);

            System.out.println("\n-----PUBLIC KEY (X.509)-----");
            System.out.println(publicKeyEncoded);


            String originalText = "Hello, 这是一条加密消息！";

            // 1. 加密
            String encryptedText = encrypt(originalText, publicKeyEncoded);
            System.out.println("加密后的密文: " + encryptedText);

            // 2. 解密
            String decryptedText = decrypt(encryptedText, privateKeyEncoded);
            System.out.println("解密后的明文: " + decryptedText);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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