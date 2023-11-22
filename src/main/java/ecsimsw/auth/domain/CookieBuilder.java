package ecsimsw.auth.domain;

public class CookieBuilder {

    private final String name;
    private final int maxAge;
    private final String path;
    private final boolean isHttpOnly;
    private final boolean isSecure;

    public CookieBuilder(String name, int maxAge, String path, boolean isHttpOnly, boolean isSecure) {
        this.name = name;
        this.maxAge = maxAge;
        this.path = path;
        this.isHttpOnly = isHttpOnly;
        this.isSecure = isSecure;
    }

    public String getName() {
        return name;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public String getPath() {
        return path;
    }

    public boolean isHttpOnly() {
        return isHttpOnly;
    }

    public boolean isSecure() {
        return isSecure;
    }
}
