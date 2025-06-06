package corsica.comiti.debloater.frames;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import corsica.comiti.debloater.enums.OS;
import corsica.comiti.debloater.interfaces.ProcessListener;
import corsica.comiti.debloater.utils.HTTPClient;
import corsica.comiti.debloater.utils.MessageBox;
import corsica.comiti.debloater.utils.ZipUtils;

public class DependsFrame extends Frame implements ProcessListener {

    private static final long serialVersionUID = 5868782603057364021L;
	private File adbDirectory;

    private final JProgressBar progressBar = new JProgressBar();

    public DependsFrame() {
        super("ADB Downloader");
        setLayout(new BorderLayout());
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);
        add(progressBar, BorderLayout.CENTER);
        setSize(400, 70);
        setLocationRelativeTo(null);
        setVisible(true);
        this.adbDirectory = downloadAdb();
    }

    private File downloadAdb() {
        File adbDirectory = new File("platform-tools");
        if (adbDirectory.exists()) {
            onOut("ADB directory already exists: %s%n", adbDirectory.getAbsolutePath());
            return adbDirectory;
        }

        if (!adbDirectory.mkdirs()) {
            onErr("Failed to create directory: %s%n", adbDirectory.getAbsolutePath());
            return null;
        }

        File zipFile = new File(adbDirectory, "platform-tools.zip");
        if (zipFile.exists() && !zipFile.delete()) {
            onErr("Failed to delete existing zip file: %s%n", zipFile.getAbsolutePath());
            return null;
        }

        OS os = OS.getOS();
        if (os == OS.OTHER) {
            onErr("No official platform-tools build is available for your system on Google's servers.%n");
            return null;
        }

        try {
            URL url = URI.create(
                    String.format("https://dl.google.com/android/repository/platform-tools-latest-%s.zip", os.toString().toLowerCase()))
                    .toURL();
            onOut("Downloading ADB platform-tools from %s%n", url);

            HTTPClient httpClient = new HTTPClient(url);
            httpClient.addProgressListener((current, total) -> {
                int percent = (total > 0) ? (int) ((current * 100L) / total) : 0;
                SwingUtilities.invokeLater(() -> {
                	progressBar.setValue(percent);
                	progressBar.revalidate();
                });
                onOut("\rDownloading %s: %d%%", zipFile.getName(), percent);
                if (percent == 100) {
                	onOut("%n");
                }
            });

            httpClient.download(url, zipFile);

            if (!zipFile.exists()) {
                onErr("Failed to download platform-tools.%n");
                cleanup(adbDirectory, zipFile);
                return null;
            }

            ZipUtils.unzip(zipFile, adbDirectory);

        } catch (IOException e) {
            onErr("Error downloading or unzipping platform-tools: %s%n", e.getMessage());
            cleanup(adbDirectory, zipFile);
            return null;
        } finally {
            if (zipFile.exists() && !zipFile.delete()) {
                onErr("Failed to delete zip file: %s%n", zipFile.getAbsolutePath());
            }
        }

        if (os == OS.LINUX || os == OS.MAC) {
            File adb = new File(adbDirectory, "adb");
            if (!adb.exists()) {
                onErr("ADB binary not found after unzip.%n");
                return null;
            }
            if (!adb.canExecute()) {
                try {
                    Path path = adb.toPath();
                    Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
                    perms.add(PosixFilePermission.OWNER_EXECUTE);
                    Files.setPosixFilePermissions(path, perms);
                    onOut("Set executable permissions for adb.%n");
                } catch (IOException e) {
                    onErr("Failed to set executable permissions on adb: %s%n", e.getMessage());
                }
            }
        }

        return adbDirectory;
    }

    private void cleanup(File adbDirectory, File zipFile) {
        if (zipFile.exists() && !zipFile.delete()) {
            onErr("Failed to delete zip file during cleanup: %s%n", zipFile.getAbsolutePath());
        }
        if (adbDirectory.exists() && !adbDirectory.delete()) {
            onErr("Failed to delete adb directory during cleanup: %s%n", adbDirectory.getAbsolutePath());
        }
    }

    @Override
    public void onOut(String out, Object... args) {
        System.out.printf(out, args);
    }

    @Override
    public void onErr(String err, Object... args) {
        String message = String.format(err, args);
        if (Desktop.isDesktopSupported()) {
            MessageBox.error(message);
        }
        System.err.print(message);
    }

    @Override
    public void onExit(int code) {
        // No action needed currently
    }

	public File getAdbDirectory() {
		return adbDirectory;
	}
	
}