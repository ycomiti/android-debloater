package corsica.comiti.debloater.components;

import javax.swing.JLabel;

public class Label extends JLabel {

	private static final long serialVersionUID = -6927308817493116991L;
	private int id;
	
	public Label(int id, String text) {
		super(text);
		this.setId(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
