package corsica.comiti.debloater.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import corsica.comiti.debloater.Main;
import corsica.comiti.debloater.adb.utils.ADB;
import corsica.comiti.debloater.adb.utils.PM;
import corsica.comiti.debloater.components.Button;
import corsica.comiti.debloater.components.ComboBox;
import corsica.comiti.debloater.components.Frame;
import corsica.comiti.debloater.constants.Links;
import corsica.comiti.debloater.interfaces.ProcessListener;
import corsica.comiti.debloater.utils.Debloater;
import corsica.comiti.debloater.utils.MessageBox;
import corsica.comiti.debloater.utils.Theme;

public class DebloaterFrame extends Frame implements ProcessListener, ActionListener {

    private static final long serialVersionUID = 5350220743350929379L;
    
    private final Dimension frameDimension = new Dimension(800, 500);

    private final JPanel menuPanel = getMenuPanel();
    private final JPanel leftTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    private final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    private final JPanel topPanelsContainer = new JPanel(new BorderLayout());
    private final JPanel headerPanel = new JPanel(new BorderLayout());
    private final ComboBox<String> deviceSelector = getComboBox(1);
    private final JLabel modelLabel = new JLabel();
	private final JLabel manufacturerLabel = new JLabel();
	private final Button startButton = getButton(2, "Start");
	private final Button refreshButton = getButton(1, "Refresh");
    private final JTextPane loggerPane = new JTextPane();
    private final JScrollPane scrollPane = new JScrollPane(loggerPane);
    private final StyledDocument doc = loggerPane.getStyledDocument();

    private final ADB adb;
    private final PM pm;

	private String manufacturer;

	private String model;

    public DebloaterFrame(File adbDirectory) {
        super("Debloater");
        this.pm = new PM(this.adb = new ADB(adbDirectory));
        adb.takeOver();
        loggerPane.setEditable(false);
        loggerPane.setFocusable(false);
        loggerPane.setBackground(UIManager.getColor("TextArea.background"));
        loggerPane.setFont(UIManager.getFont("TextArea.font"));
        
        if (Main.getInstance().isDebugging()) {
            this.adb.addProcessListener(this);	
        }
        
        reset();

        updateDeviceInfo();
        setLayout(new BorderLayout(10, 10));

        leftTopPanel.add(new JLabel("Select Device:"));
        leftTopPanel.add(deviceSelector);

        JPanel rightTopPanel = new JPanel();
        rightTopPanel.setLayout(new BoxLayout(rightTopPanel, BoxLayout.Y_AXIS));

        labelPanel.add(modelLabel);
        labelPanel.add(manufacturerLabel);

        buttonPanel.add(refreshButton);
        buttonPanel.add(startButton);

        rightTopPanel.add(labelPanel);
        rightTopPanel.add(buttonPanel);

        rightTopPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        topPanelsContainer.add(leftTopPanel, BorderLayout.WEST);
        topPanelsContainer.add(rightTopPanel, BorderLayout.EAST);

        headerPanel.add(menuPanel, BorderLayout.NORTH);
        headerPanel.add(topPanelsContainer, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        add(scrollPane, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(frameDimension);
        setMinimumSize(frameDimension);
        setLocationRelativeTo(null);
    }

	protected void updateDeviceInfo() {
    	deviceSelector.removeAllItems();
        String selectedDevice = (String)deviceSelector.getSelectedItem();
        if (selectedDevice == null) {
        	reset();
        }

        adb.setDeviceId(null);
        List<String> devices = adb.devices();
        if (devices.isEmpty()) return;
        System.out.println(devices);
        adb.setDeviceId(selectedDevice);
        String model = adb.getModel();
        String manufacturer = adb.getManufacturer();
        SwingUtilities.invokeLater(() -> {
            try {
                for (String device : devices) {
                	deviceSelector.addItem(device);
                }
                deviceSelector.revalidate();
                deviceSelector.setSelectedIndex(0);
                manufacturerLabel.setText(String.format("Manufacturer: %s", (manufacturer != null ? manufacturer : "...")));
                modelLabel.setText(String.format("Model: %s", (model != null ? model : "...")));
                manufacturerLabel.revalidate();
                modelLabel.revalidate();
    		} catch (Exception e) {}
        });
    }
	
	private void reset() {
        SwingUtilities.invokeLater(() -> {
            manufacturerLabel.setText("Manufacturer: ...");
            modelLabel.setText("Model: ...");
            manufacturerLabel.revalidate();
            modelLabel.revalidate();
        });
	}

	private JPanel getMenuPanel() {
	    JPanel menuPanel = new JPanel();
	    menuPanel.setLayout(new BorderLayout());

	    JMenuBar menuBar = new JMenuBar();
	    
	    Map<String, List<String>> menuStructure = new LinkedHashMap<>();
	    menuStructure.put("File", List.of("Pair", "DeGoogle", "Load list", "Exit"));
	    menuStructure.put("Help", List.of("Issues"));
	    menuStructure.put("Contribute", List.of("GitHub"));
	    
	    for (Map.Entry<String, List<String>> entry : menuStructure.entrySet()) {
	        JMenu menu = new JMenu(entry.getKey());
	        menu.addActionListener(this);
	        for (String text : entry.getValue()) {
	            menu.add(getMenuItem(text));
	        }
	        menuBar.add(menu);
	    }

	    menuPanel.add(menuBar, BorderLayout.NORTH);
	    return menuPanel;
	}
    
    private JMenuItem getMenuItem(String text) {
    	JMenuItem menuItem = new JMenuItem(text);
    	menuItem.addActionListener(this);
    	return menuItem;
    }

    private Button getButton(int id, String text) {
    	Dimension dimension = new Dimension(100, 34);
    	Button button = new Button(id, text);
    	button.setPreferredSize(dimension);
    	button.setMaximumSize(dimension);
    	button.addActionListener(this);
		return button;
	}

	private <T> ComboBox<T> getComboBox(int id) {
    	Dimension dimension = new Dimension(300, 34);
    	ComboBox<T> comboBox = new ComboBox<T>(id);
    	comboBox.setPreferredSize(dimension);
    	comboBox.setMaximumSize(dimension);
    	comboBox.addActionListener(this);
		return comboBox;
	}

    protected void log(final String text, final Color color) {
        SwingUtilities.invokeLater(() -> {
            Style style = loggerPane.addStyle("ColorStyle", null);
            StyleConstants.setForeground(style, color);
            try {
                doc.insertString(doc.getLength(), text + System.lineSeparator(), style);
                loggerPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onOut(String out, Object... args) {
        log(String.format(out, args), (Theme.isDarkTheme() ? Color.WHITE : Color.BLACK));
    }

    @Override
    public void onErr(String err, Object... args) {
        log(String.format(err, args), Color.RED);
    }

    @Override
    public void onExit(int code) {}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		Object source = actionEvent.getSource();
		if (source instanceof ComboBox) {
		    switch (((ComboBox<?>)source).getId()) {
			    case 1:
		        	String device = deviceSelector.getItemAt(deviceSelector.getSelectedIndex());
		        	if (device == null) return;
		        	
		            String model = adb.getModel();
		            String manufacturer = adb.getManufacturer();
		            
		            if (manufacturer != null && model != null) {
			            setManufacturer(manufacturer);
			            setModel(model);
		            }
		            return;
		    }
		    return;
		}
		if (source instanceof Button) {
			Button button = (Button)source;
			switch (button.getId()) {
				case 1:
					button.disableTemporarily(2);
					updateDeviceInfo();
			        return;
				case 2:
					button.disableTemporarily(5);
					try {
						startDebloater(URI.create(String.format("%s/refs/heads/main/%s/%s.list", Links.GITHUB_RAW, manufacturer, model)).toURL());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
					return;
			}
			return;
		}
		if (source instanceof JMenuItem) {
			switch (((JMenuItem)source).getText().toLowerCase()) {
				case "pair":
					if (this.adb == null) return;
			        SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							new PairFrame(adb).setVisible(true);
						}
					});
					break;
				case "degoogle":
					try {
						startDebloater(URI.create("https://github.com/ycomiti/android-debloat-lists/blob/main/Special/Google.list").toURL());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
					break;
				case "load list":
					File selectedFile = openFileDialog("Please select a debloat list file", "List Files (*.list)", "list");
					if (selectedFile == null) return;
					if (!selectedFile.canRead()) {
						onErr("Unable to read the file %s: Permission denied", selectedFile.getName());
						return;
					}
					startDebloater(selectedFile);
					break;
				case "exit":
					break;
				case "issues":
					browse(String.format("%s/issues", Links.GITHUB));
					break;
				case "github":
					browse(Links.GITHUB);
					break;
			}
		}
	}
	
	private void browse(String link) {
		Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(link));
            } catch (IOException | URISyntaxException e) {
                onErr("Failed to open URL: %s", e.getMessage());
            }
        } else {
        	onErr("Desktop.Action.BROWSE action is not supported on this platform.");
        }
	}

	private File openFileDialog(String title, String description, String... extensions) {
		JFileChooser fileChooser = new JFileChooser();
	    fileChooser.setDialogTitle(title);

	    FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensions);
	    fileChooser.setFileFilter(filter);

	    int userSelection = fileChooser.showOpenDialog(null);

	    if (userSelection == JFileChooser.APPROVE_OPTION) {
	        File selectedFile = fileChooser.getSelectedFile();
	        if (selectedFile.exists()) {
		        onOut("Selected file: %s", selectedFile.getAbsolutePath());
		        return selectedFile;
	        }
	    } else {
	    	onOut("File selection cancelled.");
	    }
	    return null;
	}

	private void startDebloater(File file) {
		if (!debloatWarn()) return;
		try {
			Debloater debloater = new Debloater(pm, file);
			debloater.addProcessListener(this);
			Main.getInstance().getExecutor().submit(debloater);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startDebloater(URL url) {
		if (!debloatWarn()) return;
    	if (this.manufacturer == null || this.model == null) {
    		onErr("Unable to retrieve your device's model and manufacturer, please connect your device and enable USB debugging.");
    		return;
    	}
		try {
			Debloater debloater = new Debloater(pm, url);
			debloater.addProcessListener(this);
			Main.getInstance().getExecutor().submit(debloater);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean debloatWarn() {
		boolean bool = MessageBox.warn("Are you sure you want to continue?%n" +
									   "This action is usually reversible with a factory reset, but it may cause a soft-brick on some devices.%n" +
									   "Make sure you have backed up your important data before proceeding.");
		if (!bool) onOut("Debloat operation canceled by the user.");
		return bool;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}
	
}