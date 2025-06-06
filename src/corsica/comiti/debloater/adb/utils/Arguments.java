package corsica.comiti.debloater.adb.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Arguments {
	
	private final List<String> arguments = Collections.synchronizedList(new ArrayList<>());
	
	public Arguments(String... arguments) {
		this(Arrays.asList(arguments));
	}

	public Arguments(List<String> arguments) {
		setArguments(arguments);
	}
	
	public void add(String argument) {
		this.arguments.add(argument);
	}
	
	public void remove(String argument) {
		this.arguments.remove(argument);
	}

	public void setArguments(List<String> arguments) {
		if (arguments.isEmpty()) return;
		this.arguments.clear();
		this.arguments.addAll(arguments);
	}
	
	public List<String> getArguments() {
		return new ArrayList<>(this.arguments);
	}
	
}
