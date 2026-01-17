package com.healthfamily.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AesUtil {
    public static byte[] encrypt(byte[] data, String key) throws Exception {
        byte[] k = normalizeKey(key);
        SecretKey secretKey = new SecretKeySpec(k, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
        byte[] enc = cipher.doFinal(data);
        byte[] out = new byte[iv.length + enc.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(enc, 0, out, iv.length, enc.length);
        return out;
    }

    public static byte[] decrypt(byte[] data, String key) throws Exception {
        byte[] k = normalizeKey(key);
        SecretKey secretKey = new SecretKeySpec(k, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12];
        System.arraycopy(data, 0, iv, 0, iv.length);
        byte[] enc = new byte[data.length - iv.length];
        System.arraycopy(data, iv.length, enc, 0, enc.length);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
        return cipher.doFinal(enc);
    }

    private static byte[] normalizeKey(String key) {
        byte[] src = Base64.getDecoder().decode(key);
        if (src.length == 32) return src;
        byte[] out = new byte[32];
        for (int i = 0; i < 32; i++) out[i] = i < src.length ? src[i] : 0;
        return out;
    }
}

