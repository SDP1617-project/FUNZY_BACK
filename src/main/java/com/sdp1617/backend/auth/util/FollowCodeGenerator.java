package com.sdp1617.backend.auth.util;

import java.security.SecureRandom;

public final class FollowCodeGenerator {

    private static final int LENGTH = 8;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private FollowCodeGenerator() {
    }

    public static String generate() {
        StringBuilder builder = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            builder.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return builder.toString();
    }
}
