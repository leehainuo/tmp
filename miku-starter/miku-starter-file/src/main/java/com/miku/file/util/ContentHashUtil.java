package com.miku.file.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility to compute content hash (SHA-256) while streaming data to disk.
 */
public final class ContentHashUtil {

    private ContentHashUtil() {}

    public static String computeHashAndWriteTemp(InputStream in, Path tempFile) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream dis = new DigestInputStream(in, md);
                 OutputStream os = Files.newOutputStream(tempFile)) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = dis.read(buf)) != -1) {
                    os.write(buf, 0, r);
                }
            }
            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}


