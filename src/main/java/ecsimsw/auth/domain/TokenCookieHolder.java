package ecsimsw.auth.domain;

import ecsimsw.auth.exception.SimpleAuthException;

import javax.servlet.http.Cookie;

public class TokenCookieHolder {

    private final String name;
    private final int maxAge;
    private final String path;
    private final boolean isHttpOnly;
    private final boolean isSecure;
    private final String domain;
    private final int version;
    private final String comment;

    public static TokenCookieHolderBuilder from(String name, int maxAge) {
        if (name == null) {
            throw new SimpleAuthException("Invalid token cookie value. Name can't be null");
        }
        return new TokenCookieHolderBuilder()
            .name(name)
            .maxAge(maxAge);
    }

    private TokenCookieHolder(String name, int maxAge, String path, boolean isHttpOnly, boolean isSecure, String domain, int version, String comment) {
        this.name = name;
        this.maxAge = maxAge;
        this.path = path;
        this.isHttpOnly = isHttpOnly;
        this.isSecure = isSecure;
        this.domain = domain;
        this.version = version;
        this.comment = comment;
    }

    public Cookie toCookie(String tokenValue) {
        var cookie = new Cookie(name, tokenValue);
        cookie.setPath(path);
        cookie.setHttpOnly(isHttpOnly);
        cookie.setSecure(isSecure);
        cookie.setDomain(domain);
        cookie.setVersion(version);
        cookie.setComment(comment);
        return cookie;
    }

    public static class TokenCookieHolderBuilder {

        private String name = null;
        private Integer maxAge = null;
        private String path = "/";
        private boolean isHttpOnly = true;
        private boolean isSecure = true;
        private String domain = "";
        private int version = 0;
        private String comment = "";

        public TokenCookieHolderBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TokenCookieHolderBuilder maxAge(int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public TokenCookieHolderBuilder path(String path) {
            this.path = path;
            return this;
        }

        public TokenCookieHolderBuilder httpOnly(boolean httpOnly) {
            this.isHttpOnly = httpOnly;
            return this;
        }

        public TokenCookieHolderBuilder secure(boolean secure) {
            this.isSecure = secure;
            return this;
        }

        public TokenCookieHolderBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public TokenCookieHolderBuilder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public TokenCookieHolderBuilder version(int version) {
            this.version = version;
            return this;
        }

        public TokenCookieHolder build() {
            if (name == null || maxAge == null) {
                throw new SimpleAuthException("Invalid token cookie value. Name and MaxAge can't be null");
            }
            return new TokenCookieHolder(name, maxAge, path, isHttpOnly, isSecure, domain, version, comment);
        }
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

    public String getDomain() {
        return domain;
    }

    public int getVersion() {
        return version;
    }

    public String getComment() {
        return comment;
    }
}
