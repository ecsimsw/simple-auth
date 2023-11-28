package ecsimsw.auth.domain;

import ecsimsw.auth.exception.InvalidTokenException;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "${ecsismw.token.redis.key}", timeToLive = 3600)
public class AuthTokens {

    @Id
    private final String tokenKey;
    private final String accessToken;
    private final String refreshToken;

    public AuthTokens(String tokenKey, String accessToken, String refreshToken) {
        this.tokenKey = tokenKey;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getTokenKey() {
        return tokenKey;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void requireRefreshTokenSame(String refreshToken) {
        if(!this.refreshToken.equals(refreshToken)) {
            throw new InvalidTokenException("Not registered refresh token");
        }
    }
}
