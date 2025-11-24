package com.halaq.backend.core.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

import static javax.crypto.Cipher.ENCRYPT_MODE;
import static javax.crypto.Cipher.getInstance;

public class AESUtil {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int GCM_IV_LENGTH = 12;  // 96 bits

    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // Générer un IV (Initialization Vector)
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // Configurer le chiffrement
        Cipher cipher = getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(ENCRYPT_MODE, new SecretKeySpec(key, "AES"), spec);

        // Chiffrer les données
        byte[] encryptedData = cipher.doFinal(data);

        // Retourner IV + données chiffrées
        byte[] result = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encryptedData, 0, result, iv.length, encryptedData.length);
        return result;
    }

    public static byte[] decrypt(byte[] encryptedData, byte[] key) throws Exception {
        // Extraire l'IV des données chiffrées
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, iv.length);

        // Extraire les données chiffrées
        byte[] ciphertext = new byte[encryptedData.length - GCM_IV_LENGTH];
        System.arraycopy(encryptedData, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        // Configurer le déchiffrement
        Cipher cipher = getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(2, new SecretKeySpec(key, "AES"), spec);

        // Déchiffrer les données
        return cipher.doFinal(ciphertext);
    }

}
