import miku.lib.common.util.FileUtils;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {
            File file = new File("test");
            if (!file.isDirectory()) file.mkdir();
            File file1 = new File("test/file1");
            if (!file1.exists()) file1.createNewFile();
            FileUtils.CopyDir(file, new File("test2"));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
