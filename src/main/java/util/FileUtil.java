package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileUtil {
    static List<String> result = new ArrayList<>();

    //  TODO: use new thread for IO
    public static void append(String line, String pathName) throws Exception {

        File txt = new File(pathName);
        if (!txt.exists()) {
            txt.createNewFile();
        }

        if (result.size() > 120) {
            StringBuilder sb = new StringBuilder();
            for (String s : result) {
                sb.append(s);
            }
            FileWriter fw = new FileWriter(txt, true);
            PrintWriter fos = new PrintWriter(fw);
            fos.println(sb.toString());
            fos.flush();
            fos.close();
            result.clear();
        } else {
            result.add(line);
        }
    }
}
