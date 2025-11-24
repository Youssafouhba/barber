package com.halaq.backend.core.util;

import org.apache.commons.codec.binary.Base32;

import java.security.SecureRandom;

public class OTPUtil {
    public static String generateOTP() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes).substring(0, 6);
    }
}
