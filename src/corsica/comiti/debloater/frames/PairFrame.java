package corsica.comiti.debloater.frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import corsica.comiti.debloater.Main;
import corsica.comiti.debloater.adb.utils.ADB;
import corsica.comiti.debloater.components.Button;
import corsica.comiti.debloater.components.Frame;
import corsica.comiti.debloater.components.LimitedDocument;
import corsica.comiti.debloater.components.TextField;

public class PairFrame extends Frame implements ActionListener, KeyListener {

	private static final long serialVersionUID = 5825418276804435841L;

    private final Dimension frameDimension = new Dimension(510, 150);
    private final TextField pairIpTextField = getTextField("IP", 15, 1, 200);
    private final TextField pairPortTextField = getTextField("Port", 5, 2, 80);
    private final TextField pairCodeTextField = getTextField("Code", 6, 3, 80);
    private final TextField connectIpTextField = getTextField("IP", 15, 4, 200);
    private final TextField connectPortTextField = getTextField("Port", 5, 5, 80);
    private final Button pairButton = getButton(1, "Pair");
    private final Button connectButton = getButton(2, "Connect");
	private final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
	private final JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
	private final ADB adb;
	
	public PairFrame(ADB adb) {
		super("Pair");
		this.adb = adb;
        setLayout(new BorderLayout(10, 10));
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        topPanel.add(pairIpTextField);
        topPanel.add(pairPortTextField);
        topPanel.add(pairCodeTextField);
        topPanel.add(pairButton);
        add(topPanel, BorderLayout.NORTH);
        bottomPanel.add(connectIpTextField);
        bottomPanel.add(connectPortTextField);
        bottomPanel.add(getLabel(null, 80, 34));
        bottomPanel.add(connectButton);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(frameDimension);
        setMinimumSize(frameDimension);
        setResizable(false);
        setLocationRelativeTo(null);
	}

    private TextField getTextField(String placeholder, int limit, int id, int width) {
    	Dimension dimension = new Dimension(width, 34);
    	TextField textField = new TextField(id, placeholder);
    	textField.setDocument(new LimitedDocument(limit));
    	textField.setPreferredSize(dimension);
    	textField.setMaximumSize(dimension);
    	textField.addKeyListener(this);
		return textField;
	}

    private JLabel getLabel(String text, int width, int height) {
    	Dimension dimension = new Dimension(width, height);
    	JLabel label = new JLabel(text);
    	label.setPreferredSize(dimension);
    	label.setMaximumSize(dimension);
		return label;
	}

    private Button getButton(int id, String text) {
    	Dimension dimension = new Dimension(100, 34);
    	Button button = new Button(id, text);
    	button.setPreferredSize(dimension);
    	button.setMaximumSize(dimension);
    	button.addActionListener(this);
		return button;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		if (actionEvent.getSource() instanceof Button) {
			if (adb == null) return;
			Button button = (Button)actionEvent.getSource();
			String rawIp, rawPort;
			switch (button.getId()) {
				case 1:
					rawIp = pairIpTextField.getText();
					rawPort = pairPortTextField.getText();
					String rawCode = pairCodeTextField.getText();
					if (rawIp.isEmpty() || rawPort.isEmpty() || rawCode.isEmpty()) return;
					try {
						InetAddress address = InetAddress.getByName(rawIp);
						int port = Integer.parseInt(rawPort);
						int code = Integer.parseInt(rawCode);
						Main.getInstance().getExecutor().submit(new Runnable() {
							@Override
							public void run() {
								adb.pair(address, port, code);
							}
						});
					} catch (UnknownHostException uhex) {
						uhex.printStackTrace();
					} catch (NumberFormatException nfex) {
						nfex.printStackTrace();
					}
					return;
				case 2:
					rawIp = connectIpTextField.getText();
					rawPort = connectPortTextField.getText();
					if (rawPort.isEmpty() || rawIp.isEmpty()) return;
					try {
						InetAddress address = InetAddress.getByName(rawIp);
						int port = Integer.parseInt(rawPort);
						Main.getInstance().getExecutor().submit(new Runnable() {
							@Override
							public void run() {
								adb.connect(address, port);
							}
						});
					} catch (UnknownHostException uhex) {
						uhex.printStackTrace();
					} catch (NumberFormatException nfex) {
						nfex.printStackTrace();
					}
					return;
			}
		}
	}

	protected ADB getAdb() {
		return adb;
	}

	@Override
	public void keyTyped(KeyEvent keyEvent) {}

	@Override
	public void keyPressed(KeyEvent keyEvent) {}

	@Override
	public void keyReleased(KeyEvent keyEvent) {
		if (keyEvent.getSource() instanceof TextField) {
			TextField textField = (TextField)keyEvent.getSource();
			switch (textField.getId()) {
				case 1:
					if (!connectIpTextField.getText().equals(textField.getText())) {
						connectIpTextField.setText(textField.getText());
						connectIpTextField.revalidate();
					}
					break;
			}
		}
	}
	
}
