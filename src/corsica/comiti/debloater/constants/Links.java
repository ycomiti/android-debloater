package corsica.comiti.debloater.constants;

public final class Links {

    private Links() {}
    
    public static final String GITHUB_REPO_OWNER = "ycomiti";
    public static final String GITHUB_REPO_NAME = "android-debloater";
    public static final String GITHUB_ROOT = "https://github.com";
    public static final String GITHUB = String.format("%s/%s/%s", GITHUB_ROOT, GITHUB_REPO_OWNER, GITHUB_REPO_NAME);
    
    public static final String GITHUB_RAW_ROOT = "https://raw.githubusercontent.com";
    public static final String GITHUB_RAW_REPO_NAME = "android-debloat-lists";
    public static final String GITHUB_RAW = String.format("%s/%s/%s", GITHUB_RAW_ROOT, GITHUB_REPO_OWNER, GITHUB_RAW_REPO_NAME);
    
}