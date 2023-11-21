package ecsimsw.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.security.Key;
import java.util.Arrays;
import java.util.Map;

@Service
public class AuthTokenService <T> {

    @Value("${ecsimsw.access.token.ttl.sec}")
    private int accessTokenJwtExpireTimeSec;

    @Value("${ecsimsw.refresh.token.ttl.sec}")
    private int refreshTokenJwtExpireTimeSec;

    @Value("${ecsimsw.token.payload.name}")
    private String jwtPayloadName;

    private final Key jwtSecretKey;
    private final Class<T> payloadType;
    private final AuthTokensCacheRepository authTokensCacheRepository;

    public AuthTokenService(
        String jwtSecretKey,
        AuthTokensCacheRepository authTokensCacheRepository
    ) {
        this.jwtSecretKey = JwtUtils.createSecretKey(jwtSecretKey);
        this.authTokensCacheRepository = authTokensCacheRepository;

        var parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        this.payloadType = (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    public TokenCache issueAuthTokens(String tokenKey, Map<String, Object> payload) {
        authTokensCacheRepository.deleteById(tokenKey);
        final String accessToken = createToken(payload, accessTokenJwtExpireTimeSec);
        final String refreshToken = createToken(payload, refreshTokenJwtExpireTimeSec);
        final TokenCache tokenCache = new TokenCache(tokenKey, accessToken, refreshToken);
        authTokensCacheRepository.save(tokenCache);
        return tokenCache;
    }

    public TokenCache validateAndReissue(String accessToken, String refreshToken) {
        JwtUtils.requireExpired(jwtSecretKey, accessToken);
        JwtUtils.requireLived(jwtSecretKey, refreshToken);

        final String tokenKeyFromAT = getTokenKeyValue(accessToken);
        final String tokenKeyFromRT = getTokenKeyValue(refreshToken);
        if(!tokenKeyFromAT.equals(tokenKeyFromRT)) {
            throw new IllegalArgumentException("Tokens are not from same user");
        }
        final TokenCache currentAuthToken = authTokensCacheRepository.findById(tokenKeyFromAT).orElseThrow(() -> new IllegalArgumentException("Not valid user"));
        currentAuthToken.checkSameWith(accessToken, refreshToken);

        final T payload = JwtUtils.tokenValue(jwtSecretKey, refreshToken, jwtPayloadName, payloadType, true);
        return issueAuthTokens(tokenKeyFromAT, Map.of(jwtPayloadName, payload));
    }

    private String getTokenKeyValue(String refreshToken) {
        try {
            T payload = JwtUtils.tokenValue(jwtSecretKey, refreshToken, jwtPayloadName, payloadType, true);
            final Field field = Arrays.stream(payload.getClass().getDeclaredFields())
                .filter(it -> it.isAnnotationPresent(TokenKey.class))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Payload class doesn't have TokenKey."));
            return (String) field.get(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    private String createToken(Map<String, Object> payload, int expiredTime) {
        return JwtUtils.createToken(jwtSecretKey, payload, expiredTime);
    }

    public boolean isValidToken(String token) {
        try {
            getPayloadFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public T getPayloadFromToken(String token) {
        return JwtUtils.tokenValue(jwtSecretKey, token, jwtPayloadName, payloadType);
    }
}
