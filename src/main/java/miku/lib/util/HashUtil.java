package miku.lib.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 计算文件sha256值
 *
 * @author ryz
 * @since 2020-05-12
 */
public class HashUtil {
    public static String getHash(File file, String hashType) throws IOException, NoSuchAlgorithmException {
        InputStream fis = Files.newInputStream(file.toPath());
        byte[] buffer = new byte[1024];
        MessageDigest md5 = MessageDigest.getInstance(hashType);
        for (int numRead; (numRead = fis.read(buffer)) > 0; ) {
            md5.update(buffer, 0, numRead);
        }
        fis.close();
        return toHexString(md5.digest());
    }
    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte aB : b) {
            sb.append(Integer.toHexString(aB & 0xFF));
        }
        return sb.toString();
    }
}