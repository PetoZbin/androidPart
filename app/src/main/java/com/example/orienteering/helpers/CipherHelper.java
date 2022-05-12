package com.example.orienteering.helpers;

import android.annotation.SuppressLint;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class CipherHelper {

    //spravnost hesla
    public static Boolean verifyPassword(String inputPassword, String hashedPassword){

        return BCrypt.verifyer().verify(inputPassword.toCharArray(), hashedPassword).verified;
    }

    //zahashovanie hesla a overenie: https://github.com/patrickfav/bcrypt zdroj↓↑ - kniznica bcrypt java

    public static String hashPassword(String inputPassword){

        return BCrypt.withDefaults().hashToString(12, inputPassword.toCharArray());
    }

    //zasifrovanie privatneho kluca - naucne video - zdroj: https://www.youtube.com/watch?v=kN8hlHO8US0&t=287s

    //encrytion key - uzivatelov vstup - heslo
    private static SecretKeySpec generateKeySpec(String encryptionKey) throws NoSuchAlgorithmException {

        final MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[]bytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        digest.update(bytes, 0, bytes.length);

        byte[] key = digest.digest();
        return new SecretKeySpec(key,"AES");

    }

    public static String encryptByAes(String encryptionKey, String toBeEncrypted) throws Exception {

        SecretKeySpec keySpec = generateKeySpec(encryptionKey);

        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encrypted = cipher.doFinal(toBeEncrypted.getBytes());    //bytes array forma zakodovaneho textu

        return Base64.encodeToString(encrypted, Base64.DEFAULT);    // zakodujem do retazca
    }


    // odsifrovanie privatneho kluca cez encrypt key (bol zadany uzivatelom)

    public static String decryptByAes(String encryptionKey, String toBeDecrypted) throws Exception{

        SecretKeySpec keySpec = generateKeySpec(encryptionKey);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] decoded = Base64.decode(toBeDecrypted, Base64.DEFAULT);  //hodnota je enkodovana BASE64 - dekodujem
        byte[] decrypted = cipher.doFinal(decoded);

        return new String(decrypted);   //byte array konstruktor Stringu
    }


}
