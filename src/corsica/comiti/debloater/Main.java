package corsica.comiti.debloater;

import java.awt.Desktop;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.UIManager;

import corsica.comiti.debloater.adb.utils.Arguments;
import corsica.comiti.debloater.adb.utils.Options;
import corsica.comiti.debloater.frames.DebloaterFrame;
import corsica.comiti.debloater.frames.DependsFrame;

public class Main {

    private static final Main INSTANCE = new Main();
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final boolean isDebugging = false;
    
    private Arguments arguments;
    private Options options;

    public static void main(String... args) {
        getInstance().init(args);
    }

    private void init(String... args) {
        this.arguments = new Arguments(args);
        this.options = new Options(args);
        
    	if (!Desktop.isDesktopSupported()) {
    		System.err.println("Desktop mode is currently required to use this software.");
    		return;
    	}
    	
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        DependsFrame dependsFrame = new DependsFrame();
        
        dependsFrame.dispose();
        
        new DebloaterFrame(dependsFrame.getAdbDirectory()).setVisible(true);
    }

    public Options getOptions() {
        return options;
    }

    public Arguments getArguments() {
        return arguments;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public boolean isDebugging() {
        return isDebugging;
    }

    public static Main getInstance() {
        return INSTANCE;
    }
}