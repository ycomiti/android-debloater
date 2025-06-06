package corsica.comiti.debloater.components;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

public class Button extends JButton {
    
    private static final long serialVersionUID = 131274112027064320L;
    private int id;
	private String text;
    
    public Button(int id, String text) {
        super(text);
        this.text = text;
        this.setId(id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public void disableTemporarily(int seconds) {
        SwingUtilities.invokeLater(() -> {
        	setEnabled(false);
        	revalidate();
        });

        Timer timer = new Timer();
        
        timer.scheduleAtFixedRate(new TimerTask() {
            long secondsLeft = seconds;

            @Override
            public void run() {
                if (secondsLeft >= 0) {
                	setText(String.format("%s (%d)", text, secondsLeft));
                    secondsLeft--;
                } else {
                    SwingUtilities.invokeLater(() -> {
                    	setText(text);
                    	setEnabled(true);
                    	revalidate();
                    });
                    timer.cancel();
                }
            }
        }, 0, 1000);
    }
}