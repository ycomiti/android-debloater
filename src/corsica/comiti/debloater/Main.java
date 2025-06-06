package corsica.comiti.debloater;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import corsica.comiti.debloater.adb.utils.Arguments;
import corsica.comiti.debloater.adb.utils.Options;
import corsica.comiti.debloater.enums.OS;
import corsica.comiti.debloater.frames.DebloaterFrame;
import corsica.comiti.debloater.interfaces.ProcessListener;
import corsica.comiti.debloater.utils.HTTPClient;
import corsica.comiti.debloater.utils.MessageBox;
import corsica.comiti.debloater.utils.ZipUtils;

public class Main implements ProcessListener, Runnable {

    private static final Main INSTANCE = new Main();
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final boolean isDebugging = false;

    private File adbDirectory;
    private Arguments arguments;
    private Options options;

    public static void main(String... args) {
        getInstance().init(args);
    }

    private void init(String... args) {
    	if (!Desktop.isDesktopSupported()) {
    		onErr("Desktop mode is currently required to use this software.");
    		return;
    	}
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        downloadAdb();

        this.arguments = new Arguments(args);
        this.options = new Options(args);

        SwingUtilities.invokeLater(this);
    }

    @Override
    public void run() {
        new DebloaterFrame(adbDirectory).setVisible(true);
    }

    private void downloadAdb() {
        adbDirectory = new File("platform-tools");
        if (adbDirectory.exists()) return;

        if (!adbDirectory.mkdirs()) {
            onErr("Failed to create directory: %s%n", adbDirectory);
            return;
        }

        File zipFile = new File(adbDirectory, "platform-tools.zip");
        if (zipFile.exists() && !zipFile.delete()) {
            onErr("Failed to delete existing zip file: %s%n", zipFile);
            return;
        }

        OS os = OS.getOS();
        if (os == OS.OTHER) {
            onErr("No official platform-tools build is available for your system on Google's servers.%n");
            return;
        }

        try {
            URL url = URI.create(
                String.format("https://dl.google.com/android/repository/platform-tools-latest-%s.zip", os.toString().toLowerCase())
            ).toURL();
            onOut("Downloading ADB platform-tools from %s%n", url);

            HTTPClient httpClient = new HTTPClient(url);
            httpClient.addProgressListener((current, total) -> {
                int percent = ((total > 0) ? (int) ((current * 100L) / total) : 0);
                onOut("Downloading %s: %d%%%n", zipFile.getName(), percent);
            });

            httpClient.download(url, zipFile);

            if (!zipFile.exists()) {
                onErr("Failed to download platform-tools.%n");
                if (!adbDirectory.delete()) {
                    onErr("Failed to delete directory: %s%n", adbDirectory);
                }
                return;
            }

            ZipUtils.unzip(zipFile, adbDirectory);

        } catch (IOException e) {
            onErr("Error downloading or unzipping platform-tools: %s%n", e.getMessage());
            if (zipFile.exists() && !zipFile.delete()) {
                onErr("Failed to delete zip file after error: %s%n", zipFile);
            }
            return;
        } finally {
            if (zipFile.exists() && !zipFile.delete()) {
                onErr("Failed to delete zip file: %s%n", zipFile);
            }
        }

        if (os == OS.LINUX || os == OS.MAC) {
            File adb = new File(adbDirectory, "adb");
            if (!adb.exists()) {
                onErr("ADB binary not found after unzip.%n");
                return;
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
    }

    public Options getOptions() {
        return options;
    }

    public Arguments getArguments() {
        return arguments;
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
    public void onExit(int code) {}

    public boolean isDebugging() {
        return isDebugging;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public static Main getInstance() {
        return INSTANCE;
    }
}