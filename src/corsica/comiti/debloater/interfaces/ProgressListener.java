package corsica.comiti.debloater.interfaces;

public interface ProgressListener {

    /**
     * Called to report progress.
     *
     * @param current the current progress value
     * @param total the total value representing 100% progress
     */
    void onProgress(int current, int total);
    
}