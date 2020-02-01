package com.revolut.moneytransfer.util;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

public class RandomUtils {
    public static String uniqueId() {
        return UUID.randomUUID().toString();
    }

    public static String randomString(int length) {
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        return new String(array, StandardCharsets.US_ASCII);
    }
}
