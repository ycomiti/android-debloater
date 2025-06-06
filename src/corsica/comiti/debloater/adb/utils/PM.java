package corsica.comiti.debloater.adb.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import corsica.comiti.debloater.adb.enums.PackageStatus;

/**
 * The PM class provides methods to manage application packages on an Android device
 * using the Android Debug Bridge (ADB).
 */
public class PM {

    private final ADB adb;

    /**
     * Constructs a PM instance with the specified ADB instance.
     *
     * @param adb the ADB instance to be used for package management
     */
    public PM(ADB adb) {
        this.adb = adb;
    }

    /**
     * Lists all packages with the specified status.
     *
     * @param packageStatus the status of the packages to list (e.g., enabled or disabled)
     * @return a list of package names that match the specified status
     */
    public List<String> listPackages(PackageStatus packageStatus) {
        return listPackages(packageStatus);
    }

    /**
     * Lists all packages for a specific user with the specified status.
     *
     * @param packageStatus the status of the packages to list (e.g., enabled or disabled)
     * @param user the user ID for which to list packages
     * @return a list of package names that match the specified status for the given user
     */
    public List<String> listPackages(PackageStatus packageStatus, int user) {
        List<String> packages = new ArrayList<>();
        List<String> output = null;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("pm list packages");
        if (packageStatus.equals(PackageStatus.DISABLED)) {
            stringBuilder.append(" -d");
        }

        output = getAdb().shell(stringBuilder.append(" --user ").append(user).toString());

        if (output != null && !output.isEmpty()) {
            for (String line : output) {
                if (line == null || line.isEmpty()) continue;

                String[] raw = line.split(":");
                if (raw.length != 2) continue;

                String key = raw[0].trim();
                String value = raw[1].trim();

                if (!key.equals("package")) continue;
                if (value.isEmpty()) continue;
                if (!AndroidUtils.isPackage(value)) continue;

                packages.add(value);
            }
        }
        return packages;
    }

    /**
     * Installs an APK file on the device.
     *
     * @param apk the APK file to install
     * @throws IOException if the APK file cannot be found or read
     */
    public void install(File apk) throws IOException {
        if (!apk.exists()) {
            throw new FileNotFoundException(String.format("APK file %s cannot be found.", apk.getAbsolutePath()));
        }
        if (!apk.canRead()) {
            throw new IOException(String.format("Cannot read the APK file %s. Permission denied.", apk.getAbsolutePath()));
        }
        getAdb().execute("install", apk.getAbsolutePath());
    }

    /**
     * Retrieves a map of users on the device, where the key is the user ID and the value is the user name.
     *
     * @return a map of user IDs to user names
     */
    public Map<Integer, String> getUsers() {
        List<String> output = getAdb().shell("pm list users");
        Map<Integer, String> users = new HashMap<>();

        Pattern pattern = Pattern.compile("UserInfo\\{(\\d+):([^:}]+):");

        for (String line : output) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                try {
                    int userId = Integer.parseInt(matcher.group(1));
                    String userName = matcher.group(2);
                    users.put(userId, userName);
                } catch (NumberFormatException e) {}
            }
        }

        return users;
    }

    /**
     * Uninstalls a package for a specific user.
     *
     * @param packageName the name of the package to uninstall
     * @param user the user ID for which to uninstall the package
     */
    public void uninstall(String packageName, int user) {
        uninstall(packageName, false, user);
    }

    /**
     * Uninstalls a package for a specific user, with an option to keep its data.
     *
     * @param packageName the name of the package to uninstall
     * @param keepData whether to keep the package's data after uninstallation
     * @param user the user ID for which to uninstall the package
     */
    public void uninstall(String packageName, boolean keepData, int user) {
        StringBuilder sb = new StringBuilder("pm uninstall");
        if (keepData) sb.append(" -k");
        sb.append(" --user ").append(user).append(' ').append(packageName);
        getAdb().shell(sb.toString());
    }

    /**
     * Retrieves the exit code of the last executed ADB command.
     *
     * @return the exit code of the last command
     */
    public int getExitCode() {
        return getAdb().getExitCode();
    }

    /**
     * Gets the ADB instance associated with this PM instance.
     *
     * @return the ADB instance
     */
    protected ADB getAdb() {
        return adb;
    }
}