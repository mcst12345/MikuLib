package miku.lib.common.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//From CatServer.

public class Md5Utils {
    private static final char[] hexDigits = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'};

    private static MessageDigest messagedigest = null;

    static {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String getMD5String(String s) {
        return getMD5String(s.getBytes());
    }

    public static String getFileMD5String(File file) throws IOException {
        InputStream fis = Files.newInputStream(file.toPath());
        byte[] buffer = new byte[1024];
        int numRead;
        while ((numRead = fis.read(buffer)) > 0)
            messagedigest.update(buffer, 0, numRead);
        fis.close();
        return bufferToHex(messagedigest.digest());
    }

    public static String getMD5String(byte[] bytes) {
        messagedigest.update(bytes);
        return bufferToHex(messagedigest.digest());
    }

    private static String bufferToHex(byte[] bytes) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte[] bytes, int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++)
            appendHexPair(bytes[l], stringbuffer);
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xF0) >> 4];
        char c1 = hexDigits[bt & 0xF];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }
}
