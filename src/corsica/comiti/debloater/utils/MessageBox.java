package corsica.comiti.debloater.utils;

import javax.swing.JOptionPane;

public final class MessageBox {

    private MessageBox() {}

    private static String formatMessage(final String message, final Object... args) {
        return (args == null || args.length == 0 ? message : String.format(message, args));
    }

    private static int showConfirmDialog(final String message, final String title, final int optionType, final int messageType) {
        return JOptionPane.showConfirmDialog(null, message, title, optionType, messageType);
    }

    public static boolean warn(final String message, final Object... args) {
        final String formattedMessage = formatMessage(message, args);
        int result = showConfirmDialog(formattedMessage, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return (result == JOptionPane.YES_OPTION);
    }

    public static void info(final String message, final Object... args) {
        final String formattedMessage = formatMessage(message, args);
        JOptionPane.showMessageDialog(null, formattedMessage, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(final String message, final Object... args) {
        final String formattedMessage = formatMessage(message, args);
        JOptionPane.showMessageDialog(null, formattedMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(final String message, final Object... args) {
        final String formattedMessage = formatMessage(message, args);
        int result = showConfirmDialog(formattedMessage, "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return (result == JOptionPane.YES_OPTION);
    }
    
}