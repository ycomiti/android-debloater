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

public class PM {

	private final ADB adb;

	public PM(ADB adb) {
		this.adb = adb;
	}
	
	public List<String> listPackages(PackageStatus packageStatus) {
		return listPackages(packageStatus);
	}
	
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
    
    public void install(File apk) throws IOException {
    	if (!apk.exists()) {
    		throw new FileNotFoundException(String.format("APK file %s cannot be found.", apk.getAbsolutePath()));
    	}
    	if (!apk.canRead()) {
    		throw new IOException(String.format("Cannot read the APK file %s. Permission denied.", apk.getAbsolutePath()));
    	}
    	getAdb().execute("install", apk.getAbsolutePath());
    }
    
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
    
    public void uninstall(String packageName, int user) {
    	uninstall(packageName, false, user);
    }

    public void uninstall(String packageName, boolean keepData, int user) {
        StringBuilder sb = new StringBuilder("pm uninstall");
        if (keepData) sb.append(" -k");
        sb.append(" --user ").append(user).append(' ').append(packageName);
        getAdb().shell(sb.toString());
    }
    
    public int getExitCode() {
    	return getAdb().getExitCode();
    }

	protected ADB getAdb() {
		return adb;
	}
	
}
