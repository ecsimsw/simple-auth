# simple-auth
Spring 에서 인증 토큰 처리를 도와주는 라이브러리    
Latest version : 2.0.0

</br>

## Feature 
- Access, Refresh token 생성, 쿠키에 추가 된다.
- Accees token으로 인증이 불가능한 경우 Refresh token으로 토큰을 재발행한다.
- Refresh token 탈취시 해당 토큰을 무효화할 수 있다.
- Token에 저장될 payload 타입을 라이브러리 사용자가 직접 정의할 수 있다.
- 인증이 필요한 API를 라이브러리 사용자가 지정할 수 있다.
- 인증 후 Token의 payload를 라이브러리 사용자가 바로 사용할 수 있다.

</br>

## How to configure

### 1. build.gradle
```
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.ecsimsw:simple-auth:2.0.0'
}
```

### 2. application.properties
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

### 3. Enable simple auth
``` java
@EnableSimpleAuth
@SpringBootApplication
public class SampleApplication {
}
```

### 4. 토큰 Payload 정의
``` java
public class MyLoginPayload {
    @TokenKey
    private String name;
    private String email;
```
Token에 포함될 정보를 Class로 정의한다. 타입 이름부터 프로퍼티 수, 이름, 타입 모두 커스텀 가능하다.    
단 인증에 사용될 하나의 String 타입 프로퍼티에 @TokenKey 를 붙어야 하고, 빈 생성자, Getter/Setter 가 존재해야 한다.

### 5. AuthTokenService 정의

``` java
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
            MyLoginPayload.class
        );
    }
}
```
AuthTokenService에는 Refresh 토큰 레포지토리, 토큰들의 Cookie 설정, jwt 시크릿 키, 정의한 Payload 타입 정보가 포함된다.          

TokenCookieHolder 에선 각 토큰을 값으로 발행될 쿠키 속성을 정할 수 있다.    
이름, 수명은 반드시 정의되어야 하고, isHttpOnly, isSecure의 기본 속성은 True 임을 유의한다. 

</br>

## How to use

Controller에서의 사용 예시이다. 앞서 설정한 AuthTokenService를 주입받는다.

``` java
@RestController
public class SampleController {

    @Autowired
    private AuthTokenService authTokenService;
```

### 1. 토큰 발행하기

``` java
@PostMapping("/sample/auth")
public ResponseEntity<String> login(String username, String password, HttpServletResponse response) {
    // 0. your login logic
    var userInfo = new MyLoginPayload(username, "ecsimsw@gmail.com");  // 1. Create payload 
    authTokenService.issue(response, userInfo);                        // 2. issue auth tokens
    return ResponseEntity.ok().build();                                // 3. response
}
```

사용자가 입력한 로그인 정보에 대한 확인을 마쳤다면, 앞서 정의한 커스텀 Payload 타입을 토큰에 포함될 유저 정보를 추가하여 생성한다.      
그리고 authTokenService.issue() 메서드를 payload를 인자로 호출하면 응답에 cookie 정보가 자동 추가된다.    



### 2. 사용자 인증이 필요한 API 정의 / Payload 사용하기

``` java
@GetMapping("/sample/auth")
public ResponseEntity<String> auth(@JwtPayload MyLoginPayload payload) {
    // payload 사용
    return ResponseEntity.ok(payload.getName() + " " + payload.getEmail());
}
```

로그인이 필요한 API에 파라미터로 @JwtPayload를 붙인 커스텀 Payload 타입을 추가한다.     
SimpleAuth는 이 어노테이션을 읽어 JWT를 검사하고 payload의 값을 Payload 타입에 매핑해 넣어준다.

### 3. Refresh token 무효화하기

Refresh 토큰이 탈취되었을 떄는 아래처럼 authTokenService.revoke()를 호출하는 것으로 해당 토큰이 무효화되어 더 이상 동작하지 앟도록 한다.    
이때 revoke의 기준은 tokenKey가 되는데 앞서 정의한 커스텀 Payload 타입에서 @TokenKey가 붙은 프로퍼티에 해당하는 값이다.

``` java
@DeleteMapping("/sample/auth")
public ResponseEntity<Void> revoke(String username) {
    authTokenService.revoke(username);
    return ResponseEntity.ok().build();
}
```

</br>

## Dependencies
- org.springframework.boot:spring-boot-starter-data-redis
- org.springframework.boot:spring-boot-starter-web
- io.jsonwebtoken:jjwt-api:0.11.5
- io.jsonwebtoken:jjwt-jackson:0.11.5
- io.jsonwebtoken:jjwt-impl:0.11.5


