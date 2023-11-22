package ecsimsw.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecsimsw.auth.exception.InvalidTokenException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.jsonwebtoken.lang.Maps;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

public class JwtUtils {

    private static final JacksonSerializer SERIALIZER = new JacksonSerializer(new ObjectMapper());

    public static Key createSecretKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static String createToken(Key key, Map<String, Object> payloads, int expireTime) {
        final Date now = new Date();
        final Date expiration = new Date(now.getTime() + Duration.ofSeconds(expireTime).toMillis());
        return Jwts.builder()
            .serializeToJsonWith(SERIALIZER)
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setClaims(payloads)
            .setExpiration(expiration)
            .setSubject("user-auto")
            .signWith(key)
            .compact();
    }

    public static <T> T tokenValue(Key key, String token, String claimName, Class<T> requiredType) {
        try {
            return Jwts.parserBuilder()
                .deserializeJsonWith(new JacksonDeserializer(Maps.of(claimName, requiredType).build()))
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get(claimName, requiredType);
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Is not lived token", e);
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid JWT token", e);
        }
    }
}
