package corsica.comiti.debloater.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class LimitedDocument extends PlainDocument {
	
    private static final long serialVersionUID = 7909956749902671991L;
	private final int maxLength;

    public LimitedDocument(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) return;

        if ((getLength() + str.length()) <= maxLength) {
            super.insertString(offset, str, attr);
        } else {
            int allowed = maxLength - getLength();
            if (allowed > 0) {
                super.insertString(offset, str.substring(0, allowed), attr);
            }
        }
    }
    
}