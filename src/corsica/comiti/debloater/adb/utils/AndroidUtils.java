package corsica.comiti.debloater.adb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AndroidUtils {
	
	public boolean isApk(File file) {
        if (!file.getName().toLowerCase().endsWith(".apk")) {
            return false;
        }
        try {
            if (!Files.probeContentType(file.toPath()).equals("application/vnd.android.package-archive")) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().equals("AndroidManifest.xml")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
	}
	
	public static boolean isPackage(String string) {
	    return string != null && string.matches("^[a-zA-Z_$][a-zA-Z\\d_$]*(\\.[a-zA-Z_$][a-zA-Z\\d_$]*)*$");
	}
	
}
