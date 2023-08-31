package miku.lib.common.util;

import java.io.*;
import java.nio.channels.FileChannel;

//Well, in some case (For example, on the server side) we don't have commons-io.jar, so I wrote this shit.

public class FileUtils {
    public static final long ONE_KB = 1024;
    public static final long ONE_MB = ONE_KB * ONE_KB;
    private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;

    public static void copyFile(File s, File d) throws IOException {
        if (s == null || d == null) return;
        if (!s.exists() || s.isDirectory() || s.getCanonicalPath().equals(d.getCanonicalPath())) return;
        final File parentFile = d.getParentFile();
        if (parentFile != null) {
            if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
                throw new IOException("Destination '" + parentFile + "' directory cannot be created");
            }
        }
        if (d.exists() && !d.canWrite()) {
            if (!d.setWritable(true)) throw new IOException("Destination '" + d + "' exists but is read-only");
        }
        if (d.exists() && d.isDirectory()) {
            throw new IOException("Destination '" + d + "' exists but is a directory");
        }
        try (FileInputStream fis = new FileInputStream(s); FileOutputStream fos = new FileOutputStream(d); FileChannel input = fis.getChannel(); FileChannel output = fos.getChannel()) {
            final long size = input.size(); // TODO See IO-386
            long pos = 0;
            long count;
            while (pos < size) {
                final long remain = size - pos;
                count = Math.min(remain, FILE_COPY_BUFFER_SIZE);
                final long bytesCopied = output.transferFrom(input, pos, count);
                if (bytesCopied == 0) { // IO-385 - can happen if file is truncated after caching the size
                    break; // ensure we don't loop forever
                }
                pos += bytesCopied;
            }
        }

        final long srcLen = s.length();
        final long dstLen = d.length();
        if (srcLen != dstLen) {
            throw new IOException("Failed to copy full contents from '" +
                    s + "' to '" + d + "' Expected length: " + srcLen + " Actual: " + dstLen);
        }
        d.setLastModified(s.lastModified());
    }

    public static void copyInputStreamToFile(final InputStream source, final File destination) throws IOException {
        try {
            if (destination.exists()) {
                if (destination.isDirectory()) {
                    throw new IOException("File '" + destination + "' exists but is a directory");
                }
                if (!destination.canWrite()) {
                    if (!destination.setWritable(true))
                        throw new IOException("File '" + destination + "' cannot be written to");
                }
            } else {
                final File parent = destination.getParentFile();
                if (parent != null) {
                    if (!parent.mkdirs() && !parent.isDirectory()) {
                        throw new IOException("Directory '" + parent + "' could not be created");
                    }
                }
            }
            try (final FileOutputStream output = new FileOutputStream(destination, false)) {
                int n;
                byte[] buffer = new byte[4096];
                while (EOF != (n = source.read(buffer))) {
                    output.write(buffer, 0, n);
                }
            }
        } finally {
            if (source != null) source.close();
        }
    }

    public static final int EOF = -1;

    public static void copyFileUsingFileChannels(File source, File dest) throws IOException {
        try (FileInputStream is = new FileInputStream(source)) {
            try (FileChannel ic = is.getChannel()) {
                try (FileOutputStream os = new FileOutputStream(dest)) {
                    try (FileChannel oc = os.getChannel()) {
                        oc.transferFrom(ic, 0, ic.size());
                    }
                }
            }
        }
    }

    public static void copyDir(File src, File dest) throws IOException {
        if (src == null || dest == null) return;
        if ((!src.isDirectory() && src.exists()) || (!dest.isDirectory() && dest.exists())) {
            throw new IllegalArgumentException("Failed to copy directory! One of the target exists but isn't directory!");
        }
        dest.mkdir();
        if (src.listFiles() != null) {
            for (File file : src.listFiles()) {
                if (file.isFile()) {
                    copyFile(file, new File(dest.getAbsolutePath() + File.separator + file.getName()));
                } else {
                    copyDir(file, new File(dest.getAbsolutePath() + File.separator + file.getName()));
                }
            }
        }
    }
}
