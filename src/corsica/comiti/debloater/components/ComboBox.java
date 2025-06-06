package corsica.comiti.debloater.components;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class ComboBox<E> extends JComboBox<E> {

	private static final long serialVersionUID = -5619048394858787174L;
	private int id;
	
	public ComboBox(E[] items) {
        this(0);
        setModel(new DefaultComboBoxModel<E>(items));
	}
	
	public ComboBox(int id) {
		this.setId(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
