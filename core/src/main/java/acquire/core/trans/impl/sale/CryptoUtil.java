package acquire.core.trans.impl.sale;

import android.os.Build;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {


    /**
     * 生成DES密钥
     */
    public static SecretKey generateKey(String algorithm) throws Exception {
        //密钥生成器
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
        SecureRandom random = new SecureRandom(String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));

        //可指定密钥长度
        switch (algorithm) {
            case "DES": {
                //固定长度56
                keyGenerator.init(56, random);
                break;
            }

            case "DESede": {
                //可指定密钥长度为112或168，默认为168
                // 3DES密钥长度通常是168位，但实际上会使用192位（24字节），最后24位作为奇偶校验位
                keyGenerator.init(168, random);
                break;
            }

            case "AES": {
                //这里可以是 128、192、256、越大越安全
                keyGenerator.init(256, random);
                break;
            }
            default: {
                keyGenerator.init(random);
            }
        }

        //生成key
        return keyGenerator.generateKey();
    }

    /**
     * 将密钥转成字符串
     *
     * @param secretKey
     * @return
     */
    public static String Key2Str(SecretKey secretKey) {
        //密钥数组
        byte[] key = secretKey.getEncoded();
        //转换成字符串
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * 将使用 Base64 加密后的字符串类型的 secretKey 转为 SecretKey
     *
     * @return SecretKey
     */
    public static SecretKey strKey2SecretKey(String strKey, String algorithm) {
        byte[] bytes = null;
        bytes = Base64.getDecoder().decode(strKey);
        return new SecretKeySpec(bytes, algorithm);
    }

    /**
     * 加密
     *
     * @param content   待加密内容
     * @param secretKey 加密使用的 AES 密钥
     * @return 加密后的密文 byte[]
     */
    public static byte[] encrypt(byte[] content, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(content);
    }

    /**
     * 加密
     *
     * @param content   待加密内容
     * @param secretKey 加密使用的 AES 密钥
     * @return 加密后的密文 byte[]
     */
    public static byte[] encrypt(String content, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解密
     *
     * @param content   待解密内容
     * @param secretKey 解密使用的 AES 密钥
     * @return 解密后的明文 byte[]
     */
    public static byte[] decrypt(String content, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解密
     *
     * @param content   待解密内容
     * @param secretKey 解密使用的 AES 密钥
     * @return 解密后的明文 byte[]
     */
    public static byte[] decrypt(byte[] content, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(content);
    }

    public static void main(String[] args) throws Exception {
        String algorithm = "DES";
//        String algorithm = "DESede";
//        String algorithm = "AES";

        //生成密钥key
        SecretKey key;
        //转成字符串
        String keyStr = "66B8A7AB5F3B028C7CA98CD07235E692";
        //根据字符串再生成密钥key
        key = strKey2SecretKey(keyStr, algorithm);

        //明文
        String plainText = "我是明文";

        //加密
        byte[] encryptedBytes = encrypt(plainText, key);
        String encryptedText = null;
        encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
        System.out.println("Encrypted Text: " + encryptedText);
        //解密
        byte[] decryptedBytes = decrypt(encryptedBytes, key);
        System.out.println("Decrypted Text: " + new String(decryptedBytes));
    }
}
