package util;

import java.io.File;
import java.io.FileOutputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileUtil {

    public static void save(String result, String pathname) throws Exception {
        File txt = new File(pathname);
        if (!txt.exists()) {
            txt.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(txt);
        fos.write(result.getBytes());
        fos.flush();
        fos.close();
    }
}
