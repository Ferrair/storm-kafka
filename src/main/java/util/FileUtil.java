package util;

import java.io.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileUtil {

    public static void append(String line, String pathName) throws Exception {
        File txt = new File(pathName);
        if (!txt.exists()) {
            txt.createNewFile();
        }
        FileWriter fw = new FileWriter(txt, true);
        PrintWriter fos = new PrintWriter(fw);
        fos.println(line);
        fos.flush();
        fos.close();
    }
}
