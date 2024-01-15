# simple-auth
Spring 에서 토큰 인증 처리를 도와주는 라이브러리    
Latest version : 2.0.4

## Feature 
- Access, Refresh token 생성, 쿠키에 추가 된다.
- Accees token으로 인증이 불가능한 경우 Refresh token으로 토큰을 재발행한다.
- Refresh token 탈취시 해당 토큰을 무효화할 수 있다.
- Token에 저장될 payload 타입을 라이브러리 사용자가 직접 정의할 수 있다.
- 인증이 필요한 API를 라이브러리 사용자가 지정할 수 있다.
- 인증 후 Token의 payload를 라이브러리 사용자가 바로 사용할 수 있다.

## 미리보기

``` java
@RestController
public class SampleController {

    private final AuthTokenService authTokenService;

    public SampleController(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    // 1. 토큰 생성, 쿠키 응답
    @PostMapping("/sample/auth")
    public ResponseEntity<String> login(String username, HttpServletResponse response) {
        var userInfo = new MyLoginPayload(username, "ecsimsw@gmail.com");
        authTokenService.issue(response, userInfo);     
        return ResponseEntity.ok().build();
    }

    // 2. JWT 토큰 인증, 커스텀 Payload resolve
    @GetMapping("/sample/auth")
    public ResponseEntity<String> auth(@JwtPayload MyCustomPayload info) {
        return ResponseEntity.ok(info.getName() + " " + info.getEmail());
    }

    // 3. Refresh token 무효화
    @DeleteMapping("/sample/auth")
    public ResponseEntity<Void> revoke(String username) {
        authTokenService.revoke(username);
        return ResponseEntity.ok().build();
    }
}
```

## 설정 방법

#### 1. build.gradle
```
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.ecsimsw:simple-auth:2.0.4'
}
```
#### 2. application.properties
```
# redis
spring.redis.host=${YOUR_REDIS_HOST_URL}
spring.redis.port=${YOUR_REDIS_PORT}

# token
ecsismw.token.redis.key=auth_token
ecsimsw.access.token.ttl.sec=1800
ecsimsw.refresh.token.ttl.sec= 172800
ecsimsw.token.payload.name=myTokenPayload
ecsimsw.token.secret.key=ecsimswtemptokensecretqwertyqwerty123123123
```

#### 3. 토큰 Payload에 담을 내용 정의
``` java
public class MyCustomPayload {
    @TokenKey
    private String name;
    private String email;
```
Token에 포함될 정보를 Class로 정의한다. 타입 이름부터 프로퍼티 수, 이름, 타입 모두 커스텀 가능하다.    
단 인증에 사용될 하나의 String 타입 프로퍼티에 @TokenKey 를 붙어야 하고, 빈 생성자, Getter/Setter 가 존재해야 한다.

#### 4. @EnableSimpleAuth, AuthTokenService 설정

``` java
@EnableSimpleAuth
@Configuration
public class AuthTokenConfig implements WebMvcConfigurer {

    @Bean
    public AuthTokenService authTokenService(
        @Value("${ecsimsw.token.secret.key}") String jwtSecretKey,
        @Autowired AuthTokensCacheRepository authTokensCacheRepository
    ) {
        var atCookieHolder = TokenCookieHolder.from("at", 1800).secure(false).build();
        var rtCookieHolder = TokenCookieHolder.from("rt", 172800).secure(false).build();
        return new AuthTokenService(
            jwtSecretKey,
            authTokensCacheRepository,
            atCookieHolder,
            rtCookieHolder,
            MyCustomPayload.class
        );
    }
}
```
AuthTokenService에는 Refresh 토큰 레포지토리, 토큰들의 Cookie 설정, jwt 시크릿 키, 정의한 Payload 타입 정보가 포함된다.          

TokenCookieHolder 에선 각 토큰을 값으로 발행될 쿠키 속성을 정할 수 있다.    
이름, 수명은 반드시 정의되어야 하고, isHttpOnly, isSecure의 기본 속성은 True 임을 유의한다. 

