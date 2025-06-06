package corsica.comiti.debloater.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import corsica.comiti.debloater.adb.enums.PackageStatus;
import corsica.comiti.debloater.adb.utils.AndroidUtils;
import corsica.comiti.debloater.adb.utils.PM;
import corsica.comiti.debloater.enums.ListType;
import corsica.comiti.debloater.interfaces.ProcessListener;

public class Debloater implements Runnable, ProcessListener {

    private final PM pm;
    private final ListType listType;
    private HTTPClient httpClient;
	private File debloatList;
    private final List<ProcessListener> processListeners = Collections.synchronizedList(new ArrayList<>());

    public Debloater(PM pm, File debloatList) throws IOException {
    	if (!debloatList.getAbsolutePath().endsWith(".list")) {
    		throw new IOException(String.format("Invalid file %s.", debloatList.getAbsolutePath()));
    	}
    	if (!debloatList.isFile()) {
    		throw new IOException(String.format("File %s is not a regular file.", debloatList.getAbsolutePath()));
    	}
    	this.listType = ListType.FILE;
        this.pm = pm;
        this.setDebloatList(debloatList);
    }

    public Debloater(PM pm, URL debloatList) throws IOException {
    	if (!debloatList.getPath().endsWith(".list")) {
    		throw new IOException(String.format("Invalid URL %s.", debloatList.getPath()));
    	}
    	this.listType = ListType.ONLINE;
    	this.setHttpClient(new HTTPClient(debloatList));
        this.pm = pm;
    }

	@Override
	public void run() {
        List<String> bloatwarePackages = fetchBloatwarePackages();
        if (bloatwarePackages == null || bloatwarePackages.isEmpty()) return;
    	Map<Integer, String> users = pm.getUsers();
    	for (Entry<Integer, String> entry : users.entrySet()) {
    		int user = entry.getKey();
            List<String> installedPackages = pm.listPackages(PackageStatus.INSTALLED, user);
            for (int i = 0; i < bloatwarePackages.size(); i++) {
                String packageName = bloatwarePackages.get(i);
            	if (!installedPackages.contains(packageName)) {
            		onOut("Package %s is not installed for user %d.", packageName, user);
            		continue;
            	}
                try {
                	onOut("Uninstalling package %s...", packageName);
                    pm.uninstall(packageName, true, entry.getKey());
    			} catch (Exception e) {
    				onErr("Failed to uninstall %s: %s", packageName, e.getMessage());
    			} finally {
                	onExit(pm.getExitCode());
				}
            }
    	}
	}

    public List<String> fetchBloatwarePackages() {
        List<String> result = new ArrayList<>();
        String content = null;

        try {
        	switch (getListType()) {
				case ONLINE:
	                content = getHttpClient().fetch();
					break;
				case FILE:
	        		content = fromFile(getDebloatList());
					break;
				default:
					onErr("Unsupported list type.");
					return null;
        	}
        } catch (IOException e) {
        	onErr("Failed to %s debloat list: %s", e.getMessage(), (getListType() == ListType.ONLINE ? "fetch" : "load"));
            return null;
        }

        if (content == null || content.isEmpty()) return null;

        try (Scanner scanner = new Scanner(content)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (AndroidUtils.isPackage(line)) {
                    result.add(line);
                }
            }
        } catch (Exception e) {
            System.err.printf("Failed to parse debloat list: %s%n", e.getMessage());
        }

        return result;
    }
    
    public ListType getListType() {
		return this.listType;
    }

	protected String fromFile(File debloatList) throws IOException {
		if (!getDebloatList().exists()) return null;
		try (Scanner scanner = new Scanner(debloatList)) {
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while (scanner.hasNextLine() && (line = scanner.nextLine()) != null) {
				stringBuilder.append(line).append(System.getProperty("line.separator"));
			}
			return stringBuilder.toString();
		}
	}

	protected final File getDebloatList() {
		return debloatList;
	}

	protected final void setDebloatList(File debloatList) {
		this.debloatList = debloatList;
	}

	protected final HTTPClient getHttpClient() {
		return httpClient;
	}

	protected final void setHttpClient(HTTPClient httpClient) {
		this.httpClient = httpClient;
	}
    
    public void addProcessListener(ProcessListener processListener) {
		this.processListeners.add(processListener);
    }
    
    public void removeProcessListener(ProcessListener processListener) {
		this.processListeners.remove(processListener);
    }

	public List<ProcessListener> getProcessListeners() {
		return new ArrayList<>(this.processListeners);
	}

	@Override
	public void onOut(String out, Object... args) {
    	for (ProcessListener processListener : getProcessListeners()) {
    		processListener.onOut(out, args);
    	}
	}

	@Override
	public void onErr(String err, Object... args) {
    	for (ProcessListener processListener : getProcessListeners()) {
    		processListener.onErr(err, args);
    	}
	}

	@Override
	public void onExit(int code) {
		if (code == 0) return;
    	for (ProcessListener processListener : getProcessListeners()) {
    		processListener.onExit(code);
    	}
	}
	
}