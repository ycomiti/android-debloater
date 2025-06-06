package corsica.comiti.debloater.adb.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Options {

	private final String separator;
	private final Map<String, String> options = Collections.synchronizedMap(new HashMap<>());
	
	public Options(String... arguments) {
		this(Arrays.asList(arguments));
	}
	
	public Options(List<String> arguments) {
		this("=", arguments);
	}
	
	public Options(String separator, String... arguments) {
		this(separator, Arrays.asList(arguments));
	}
	
	public Options(String separator, List<String> arguments) {
		if (separator == null || separator.isEmpty()) {
			throw new NullPointerException("Argument separator cannot be null or empty.");
		}
		this.separator = separator;
		HashMap<String, String> options = new HashMap<>();
		for (String argument : arguments) {
			String[] raw = argument.split(getSeparator());
			if (raw.length != 2) continue;
			options.put(raw[0], raw[1]);
		}
		setOptions(options);
	}

	public Options(Map<String, String> options) {
		this.separator = "";
		setOptions(options);
	}
	
	public void add(String key, String value) {
		this.options.put(key, value);
	}
	
	public void remove(String key) {
		this.options.remove(key);
	}

	public void setOptions(Map<String, String> options) {
		if (options.isEmpty()) return;
		this.options.clear();
		this.options.putAll(options);
	}
	
	public Map<String, String> getOptions() {
		return new HashMap<>(this.options);
	}
	
	public List<String> getKeys() {
		return new ArrayList<>(getOptions().keySet());
	}
	
	public List<String> getValues() {
		return new ArrayList<>(getOptions().values());
	}

	public String getSeparator() {
		return separator;
	}
	
}