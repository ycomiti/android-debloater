package corsica.comiti.debloater.enums;

public enum OS {

    WINDOWS,
    MAC,
    LINUX,
    OTHER;

    private static volatile OS detectedOS;

    public static OS getOS() {
        if (detectedOS == null) {
            synchronized (OS.class) {
                if (detectedOS == null) {
                    String osName = System.getProperty("os.name").toLowerCase();
                    if (osName.contains("win")) {
                        detectedOS = WINDOWS;
                    } else if (osName.contains("mac")) {
                        detectedOS = MAC;
                    } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                        detectedOS = LINUX;
                    } else {
                        detectedOS = OTHER;
                    }
                }
            }
        }
        return detectedOS;
    }

    public String getDownloadId() {
        return switch (this) {
            case WINDOWS -> "windows";
            case MAC -> "darwin";
            case LINUX -> "linux";
            default -> null;
        };
    }
}