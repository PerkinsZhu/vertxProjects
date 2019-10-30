package com.perkins.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AESUtils {
    public static String ENAES1(String data, String password) {
        try {
            byte[] dataArray = data.getBytes();
            KeyGenerator keyGene = KeyGenerator.getInstance("AES");
            keyGene.init(128, new SecureRandom(dataArray));
            SecretKey key = keyGene.generateKey();
            byte[] bytes = key.getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(bytes, "AES");
            Cipher ciper = Cipher.getInstance("AES");
            ciper.init(Cipher.ENCRYPT_MODE, keySpec);
            bytes = ciper.doFinal(dataArray);
            String result = org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String DEAES1(String str, String password) {
        try {
            byte[] srcBytes = org.apache.commons.codec.binary.Base64
                    .decodeBase64(str);
            KeyGenerator keyGene = KeyGenerator.getInstance("AES");
            keyGene.init(128, new SecureRandom(password.getBytes()));
            SecretKey key = keyGene.generateKey();
            byte[] bytes = key.getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(bytes, "AES");
            Cipher ciper = Cipher.getInstance("AES");
            ciper.init(Cipher.DECRYPT_MODE, keySpec);

            bytes = ciper.doFinal(srcBytes);
            String strs = new String(bytes);

            return strs;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    static String password = "123456785555";

    public static String DEAES(String str) {
        try {
            byte[] srcBytes = org.apache.commons.codec.binary.Base64
                    .decodeBase64(str);
            KeyGenerator keyGene = KeyGenerator.getInstance("AES");
            keyGene.init(128, new SecureRandom(password.getBytes()));
            SecretKey key = keyGene.generateKey();
            byte[] bytes = key.getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(bytes, "AES");
            Cipher ciper = Cipher.getInstance("AES");
            ciper.init(Cipher.DECRYPT_MODE, keySpec);

            bytes = ciper.doFinal(srcBytes);
            String strs = new String(bytes);

            return strs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String ENAES(String data) {
        try {
            KeyGenerator keyGene = KeyGenerator.getInstance("AES");
            keyGene.init(128, new SecureRandom(password.getBytes()));
            SecretKey key = keyGene.generateKey();
            byte[] bytes = key.getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(bytes, "AES");
            Cipher ciper = Cipher.getInstance("AES");
            ciper.init(Cipher.ENCRYPT_MODE, keySpec);

            bytes = ciper.doFinal(data.getBytes());
            String result = org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
      /*  String data = "123";
        String password = "1111";
        String temp = ENAES(data, password);
        System.out.println(temp);
        String aa = DEAES(temp, password);
        System.out.println(aa);*/

        String data = "w我的电话号码是：412325182303135417 sss 440204197911113613谁家 522528199008133616哈哈哈 32031177070600122撒的发生 320311770706002AAAA 320311770706002333 320311770706001BBBB 3203117707060012222 我的邮箱是：1245564@qq.com 我的邮箱是：12-we45564@qq.com 我的邮箱是：jd-l1245564@qq.com.cn 我的邮箱是：kw.we-1245564@qq.com";
        System.out.println("DES ENCRYPT:" + ENAES(data));
        System.out.println("DES DECRYPT:" + DEAES(ENAES(data)));
    }

}
