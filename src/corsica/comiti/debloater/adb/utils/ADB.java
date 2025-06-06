package corsica.comiti.debloater.adb.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import corsica.comiti.debloater.adb.enums.Partition;
import corsica.comiti.debloater.interfaces.ProcessListener;

/**
 * The ADB class provides methods to interact with the Android Debug Bridge (ADB)
 * for managing Android devices and executing ADB commands.
 */
public class ADB {

    private final File directory, bin;
    private final List<ProcessListener> processListeners = Collections.synchronizedList(new ArrayList<>());
    private String deviceId;
    private int exitCode;

    /**
     * Constructs an ADB instance with the specified directory for the ADB binary.
     *
     * @param directory the directory where the ADB binary is located
     */
    public ADB(File directory) {
        this.directory = directory;
        this.bin = new File(directory, (System.getProperty("os.name").toLowerCase().startsWith("windows") ? "adb.exe" : "adb"));
    }

    /**
     * Takes over the ADB server by killing it and listing connected devices.
     */
    public void takeOver() {
        killServer();
        devices();
    }

    /**
     * Kills the ADB server.
     */
    public void killServer() {
        execute("kill-server");
    }

    /**
     * Pairs the device with the specified address and port using the given pairing code.
     *
     * @param address the IP address of the device to pair with
     * @param port the port number for the pairing
     * @param code the pairing code
     */
    public void pair(InetAddress address, int port, int code) {
        execute("pair", String.format("%s:%d", address.getHostAddress(), port), String.valueOf(code));
    }

    /**
     * Connects to a device at the specified address and port.
     *
     * @param address the IP address of the device to connect to
     * @param port the port number for the connection
     */
    public void connect(InetAddress address, int port) {
        execute("connect", String.format("%s:%d", address.getHostAddress(), port));
    }

    /**
     * Executes a shell command on the connected device.
     *
     * @param command the shell command to execute
     * @return a list of output lines from the command execution
     */
    public List<String> shell(String command) {
        return execute("shell", command);
    }

    /**
     * Reboots the connected device.
     */
    public void reboot() {
        shell("reboot");
    }

    /**
     * Reboots the connected device into the specified partition.
     *
     * @param partition the partition to reboot into
     */
    public void reboot(Partition partition) {
        execute("reboot", partition.getPartition());
    }

    /**
     * Retrieves the model of the connected device.
     *
     * @return the device model
     */
    public String getModel() {
        return getProp("ro.product.model");
    }

    /**
     * Retrieves the manufacturer of the connected device.
     *
     * @return the device manufacturer
     */
    public String getManufacturer() {
        return getProp("ro.product.manufacturer");
    }

    /**
     * Retrieves the value of a specific system property.
     *
     * @param key the key of the property to retrieve
     * @return the value of the specified property, or null if not found
     */
    public String getProp(String key) {
        Map<String, String> prop = getProp();
        if (prop == null || prop.isEmpty()) return null;
        return prop.get(key);
    }

    /**
     * Retrieves all system properties as a map of key-value pairs.
     *
     * @return a map of system properties
     */
    public Map<String, String> getProp() {
        Map<String, String> prop = new HashMap<>();
        List<String> output = shell("getprop");
        if (output != null && !output.isEmpty()) {
            for (String line : output) {
                if (line == null || line.isEmpty()) continue;

                int splitIndex = line.indexOf("]: [");
                if (splitIndex == -1) continue;

                String key = line.substring(1, splitIndex);
                String value = line.substring(splitIndex + 4, line.length() - 1);

                prop.put(key.trim(), value.trim());
            }
        }
        return prop;
    }

    /**
     * Lists all connected devices.
     *
     * @return a list of device IDs for all connected devices
     */
    public List<String> devices() {
        List<String> lines = execute("-d", "devices");
        List<String> deviceIds = new ArrayList<>();

        if (lines == null || lines.isEmpty()) return deviceIds;

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            if (parts.length >= 2 && parts[1].equals("device")) {
                deviceIds.add(parts[0]);
            }
        }
        return deviceIds;
    }

    /**
     * Executes an ADB command with the specified arguments.
     *
     * @param args the arguments for the ADB command
     * @return a list of output lines from the command execution, or null if an error occurs
     */
    protected List<String> execute(String... args) {
        if (!bin.exists()) {
            System.err.println("ADB binary file cannot be found.");
            return null;
        }

        List<String> command = new ArrayList<>();
        command.add(bin.getAbsolutePath());

        if (getDeviceId() != null) {
            command.add("-s");
            command.add(getDeviceId());
        }

        for (String arg : args) {
            command.add(arg);
        }

        try {
            return runProcess(command);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Runs a process with the specified command and captures its output.
     *
     * @param command the command to execute
     * @return a list of output lines from the process execution
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    protected List<String> runProcess(List<String> command) throws IOException, InterruptedException {
        this.exitCode = -1;
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(directory);
        builder.redirectErrorStream(true);

        Process process = builder.start();

        List<String> output = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            List<ProcessListener> processListeners = getProcessListeners();
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
                for (ProcessListener processListener : processListeners) {
                    processListener.onOut(line);
                }
            }
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
                for (ProcessListener processListener : getProcessListeners()) {
                    processListener.onErr(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int exitCode = process.waitFor();

        for (ProcessListener processListener : getProcessListeners()) {
            processListener.onExit(exitCode);
        }

        if (exitCode != 0) {
            this.exitCode = exitCode;
            throw new RuntimeException(String.format("ADB command failed with exit code %d.", exitCode));
        }

        return output;
    }

    /**
     * Gets the device ID currently set for ADB commands.
     *
     * @return the device ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device ID for ADB commands.
     *
     * @param deviceId the device ID to set
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Adds a process listener to receive output and error messages from ADB commands.
     *
     * @param processListener the process listener to add
     */
    public void addProcessListener(ProcessListener processListener) {
        this.processListeners.add(processListener);
    }

    /**
     * Removes a process listener.
     *
     * @param processListener the process listener to remove
     */
    public void removeProcessListener(ProcessListener processListener) {
        this.processListeners.remove(processListener);
    }

    /**
     * Gets the exit code of the last executed ADB command.
     *
     * @return the exit code of the last command
     */
    public int getExitCode() {
        return this.exitCode;
    }

    /**
     * Gets a copy of the list of process listeners.
     *
     * @return a list of process listeners
     */
    public List<ProcessListener> getProcessListeners() {
        return new ArrayList<>(this.processListeners);
    }
}