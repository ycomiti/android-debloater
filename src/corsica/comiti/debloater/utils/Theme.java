package corsica.comiti.debloater.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.UIManager;

public final class Theme {
	
	private Theme() {}

	public static boolean isDarkTheme() {
	    Color bg = UIManager.getColor("Panel.background");
	    
	    if (bg == null) bg = UIManager.getColor("control");
	    if (bg == null) bg = Color.WHITE;
	    
	    double luminance = (0.2126 * bg.getRed() + 0.7152 * bg.getGreen() + 0.0722 * bg.getBlue());
	    return (luminance < 128);
	}
	
	public static void setAntiAliasing(Graphics2D graphics2d) {
		graphics2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

}
