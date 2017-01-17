package cc.colorcat.netbirddemo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cc.colorcat.netbird.util.LogUtils;

/**
 * Created by cxx on 16-4-19.
 * xx.ch@outlook.com
 */
@SuppressWarnings("UnusedDeclaration")
public class CryptoTool {
    private static final byte[] IV;
    // IvParameterSpec 为线程安全的类
    private static final IvParameterSpec SPEC;

    private static final String KEY_DECRYPT;
    // SecretKeySpec 为线程安全的类
    private static final SecretKeySpec KEY_DE_SECRET;

    private static final String KEY_ENCRYPT;
    private static final SecretKeySpec KEY_EN_SECRET;

    static {
        IV = "1234567890123456".getBytes();
        SPEC = new IvParameterSpec(IV);

        KEY_DECRYPT = "be7E&Ki_teo&TT&A";
        KEY_DE_SECRET = new SecretKeySpec(KEY_DECRYPT.getBytes(), "AES");

        KEY_ENCRYPT = "^fSE_DF*EMCu&emc";
        KEY_EN_SECRET = new SecretKeySpec(KEY_ENCRYPT.getBytes(), "AES");
    }

    /**
     * 解密，如果解密失败则原样返回
     */
    public static String decryptByDefault(String resource) {
        String result = resource;
        if (resource != null) {
            String res = resource;
            try {
                // 判断是否需要URLDecode
                if (res.contains("%2B") || res.contains("%3D")) {
                    res = URLDecoder.decode(res, "UTF-8");
                }
                byte[] base = Base64.decode(res.getBytes("UTF-8"), Base64.DEFAULT);
                byte[] decryptedData = newDecryptCipher().doFinal(base);
                result = new String(decryptedData, "UTF-8");
            } catch (Exception e) {
                LogUtils.e(e);
            }
        }
        return result;
    }

    // Cipher 非线程安全，在使用时需获取新的实例
    private static Cipher newDecryptCipher() throws GeneralSecurityException {
        Cipher result = Cipher.getInstance("AES/CBC/PKCS5Padding");
        result.init(Cipher.DECRYPT_MODE, KEY_DE_SECRET, SPEC);
        return result;
    }

    /**
     * 加密，如果加密失败则原样返回
     */
    public static String encryptByDefault(String resource) {
        String result = resource;
        if (resource != null) {
            try {
                byte[] encryptedData = newEncryptCipher().doFinal(resource.getBytes("UTF-8"));
                String base64Str = new String(Base64.encode(encryptedData, Base64.DEFAULT), "UTF-8");
                result = URLEncoder.encode(base64Str, "UTF-8");
            } catch (Exception e) {
                LogUtils.e(e);
            }
        }
        return result;
    }

    // Cipher 非线程安全，在使用时需获取新的实例
    private static Cipher newEncryptCipher() throws GeneralSecurityException {
        Cipher result = Cipher.getInstance("AES/CBC/PKCS5Padding");// 算法/模式/补码方式
        result.init(Cipher.ENCRYPT_MODE, KEY_EN_SECRET, SPEC);
        return result;
    }

    @Nullable
    public static String decrypt(@NonNull String resource, @NonNull String key) {
        String result = null;
        String res = resource;
        try {
            // 判断是否需要URLDecode
            if (res.contains("%2B") || res.contains("%3D")) {
                res = URLDecoder.decode(res, "UTF-8");
            }
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, SPEC);
            byte[] base64Byte = Base64.decode(res.getBytes("UTF-8"), Base64.DEFAULT);
            byte[] decryptedData = cipher.doFinal(base64Byte);
            result = new String(decryptedData, "UTF-8");
        } catch (Exception e) {
            LogUtils.e(e);
        }
        return result;
    }

    public static String encrypt(@NonNull String encryptString, @NonNull String encryptKey) throws Exception {
        byte[] keys = encryptKey.getBytes();
        SecretKeySpec key = new SecretKeySpec(keys, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 算法/模式/补码方式
        cipher.init(Cipher.ENCRYPT_MODE, key, SPEC);
        byte[] encryptedData = cipher.doFinal(encryptString.getBytes("UTF-8"));
        String base64Str = new String(Base64.encode(encryptedData, Base64.DEFAULT), "UTF-8");
        return URLEncoder.encode(base64Str, "UTF-8");
    }

    /**
     * md5 加密，如果加密失败则原样返回
     */
    public static String md5(@NonNull String resource) {
        String result = resource;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(resource.getBytes());
            byte[] bytes = digest.digest();
            int len = bytes.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(b & 0x0f, 16));
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            LogUtils.e(e);
        }
        return result;
    }
}
