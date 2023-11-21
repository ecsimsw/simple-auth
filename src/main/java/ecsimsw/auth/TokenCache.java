package ecsimsw.auth;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "TOKEN_CACHE", timeToLive = 60 * 3)
public class TokenCache {

    @Id
    private final String tokenKey;
    private final String accessToken;
    private final String refreshToken;

    public TokenCache(String tokenKey, String accessToken, String refreshToken) {
        this.tokenKey = tokenKey;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public void checkSameWith(String accessToken, String refreshToken) {
        if(this.accessToken.equals(accessToken) && this.refreshToken.equals(refreshToken)) {
            return;
        }
        throw new IllegalArgumentException("Not sync with cached auth tokens");
    }
}
