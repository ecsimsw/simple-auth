package ecsimsw.usage.sample;

import ecsimsw.auth.TokenKey;

public class LoginUserInfo {

    @TokenKey
    private final String username;
    private final Long userId;
    private final String userEmail;

    public LoginUserInfo(String username, Long userId, String userEmail) {
        this.username = username;
        this.userId = userId;
        this.userEmail = userEmail;
    }

    public String getUsername() {
        return username;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }
}
