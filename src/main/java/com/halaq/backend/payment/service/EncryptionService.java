package com.halaq.backend.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.security.AlgorithmParameters;
import java.util.Base64;

@Service
@Slf4j
public class EncryptionService {
    @Value("${encryption.key}")
    private String encryptionKey;

    @Value("${encryption.algorithm}")
    private String algorithm;

    @Value("${encryption.transformation}")
    private String transformation;

    public String encrypt(String plainText) throws Exception {
        SecretKey key = generateKey();
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        AlgorithmParameters params = cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

        byte[] encryptedText = cipher.doFinal(plainText.getBytes());

        // Combiner IV + encrypted text
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(iv);
        outputStream.write(encryptedText);

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    public String decrypt(String encryptedText) throws Exception {
        SecretKey key = generateKey();

        byte[] decodedText = Base64.getDecoder().decode(encryptedText);

        // Extraire IV
        IvParameterSpec iv = new IvParameterSpec(decodedText, 0, 16);

        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        byte[] decryptedText = cipher.doFinal(decodedText, 16, decodedText.length - 16);

        return new String(decryptedText);
    }

    private SecretKey generateKey() throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(encryptionKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, algorithm);
    }
}

