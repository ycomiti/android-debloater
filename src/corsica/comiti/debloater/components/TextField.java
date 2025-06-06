package corsica.comiti.debloater.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JTextField;

import corsica.comiti.debloater.utils.Theme;

public class TextField extends JTextField {

	private static final long serialVersionUID = -6927308817493116991L;

	private int id;
	private String placeholder;
	private Color placeholderColor = (Theme.isDarkTheme() ? Color.GRAY : Color.LIGHT_GRAY);
	
	public TextField() {
		this(0);
	}

	public TextField(int id) {
		this(0, null);
	}

	public TextField(String placeholder) {
		this(0, placeholder);
	}

	public TextField(int id, String placeholder) {
		this.placeholder = placeholder;
		this.id = id;
		setCaretColor(Theme.isDarkTheme() ? Color.GRAY : Color.BLACK);
		setForeground(Theme.isDarkTheme() ? Color.WHITE : Color.BLACK);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
		repaint();
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholderColor(Color color) {
		this.placeholderColor = color;
		repaint();
	}

	public Color getPlaceholderColor() {
		return placeholderColor;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if ((getText() == null || getText().isEmpty()) && placeholder != null && !hasFocus()) {
			Graphics2D g2 = (Graphics2D)g.create();
			Theme.setAntiAliasing(g2);
			g2.setColor(placeholderColor);
			g2.setFont(getFont());
			FontMetrics fm = g2.getFontMetrics();
			int x = getInsets().left;
			int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
			g2.drawString(placeholder, x, y);
			g2.dispose();
		}
	}
}