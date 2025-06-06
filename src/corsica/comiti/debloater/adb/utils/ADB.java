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

public class ADB {

    private final File directory, bin;
    private final List<ProcessListener> processListeners = Collections.synchronizedList(new ArrayList<>());
    private String deviceId;
	private int exitCode;

    public ADB(File directory) {
        this.directory = directory;
        this.bin = new File(directory, (System.getProperty("os.name").toLowerCase().startsWith("windows") ? "adb.exe" : "adb"));
    }
    
    public void takeOver() {
    	killServer();
    	devices();
    }
    
    public void killServer() {
    	execute("kill-server");
    }
    
    public void pair(InetAddress address, int port, int code) {
        execute("pair", String.format("%s:%d", address.getHostAddress(), port), String.valueOf(code));
    }
    
    public void connect(InetAddress address, int port) {
        execute("connect", String.format("%s:%d", address.getHostAddress(), port));
    }

    public List<String> shell(String command) {
        return execute("shell", command);
    }
    
    public void reboot() {
    	shell("reboot");
    }
    
    public void reboot(Partition partition) {
    	execute("reboot", partition.getPartition());
    }
    
    public String getModel() {
        return getProp("ro.product.model");
    }

    public String getManufacturer() {
        return getProp("ro.product.manufacturer");
    }

    public String getProp(String key) {
        Map<String, String> prop = getProp();
        if (prop == null || prop.isEmpty()) return null;
        return prop.get(key);
    }
    
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
    
    public List<String> devices() {
        List<String> lines = execute("-d", "devices");
        List<String> deviceIds = new ArrayList<>();
        
        if (lines == null || lines.isEmpty()) return deviceIds;
        
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            if (parts.length >= 2 && "device".equals(parts[1])) {
                deviceIds.add(parts[0]);
            }
        }
        return deviceIds;
    }
    
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

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
    
    public void addProcessListener(ProcessListener processListener) {
		this.processListeners.add(processListener);
    }
    
    public void removeProcessListener(ProcessListener processListener) {
		this.processListeners.remove(processListener);
    }
    
    public int getExitCode() {
    	return this.exitCode;
    }

	public List<ProcessListener> getProcessListeners() {
		return new ArrayList<>(this.processListeners);
	}
	
}