package com.github.lbovolini.crowd.group;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MulticastMessageUtils {

    public static String getMessage(ByteBuffer buffer) {
        byte[] buff = new byte[buffer.limit()];
        buffer.get(buff, 0, buffer.limit());
        buffer.clear();
        return new String(buff, StandardCharsets.UTF_8);
    }
}
