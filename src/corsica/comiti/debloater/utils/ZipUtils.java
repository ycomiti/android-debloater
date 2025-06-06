package corsica.comiti.debloater.utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ZipUtils {
	
	private ZipUtils() {}
	
	public static void unzip(File zipFile, File destDir) throws IOException {
	    if (!destDir.exists() && !destDir.mkdirs()) {
	        throw new IOException(String.format("Failed to create directory %s", destDir));
	    }

	    try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
	        ZipEntry entry;
	        while ((entry = zis.getNextEntry()) != null) {
	            if (entry.isDirectory()) continue;

	            String fileName = new File(entry.getName()).getName();
	            File outFile = new File(destDir, fileName);

	            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile))) {
	                byte[] buffer = new byte[4096];
	                int len;
	                while ((len = zis.read(buffer)) > 0) {
	                    bos.write(buffer, 0, len);
	                }
	            }

	            zis.closeEntry();
	        }
	    }
	}
}