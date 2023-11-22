package ecsimsw.usage.sample;

import ecsimsw.auth.AuthTokenService;
import ecsimsw.auth.LoginUser;
import ecsimsw.auth.AuthTokens;
import ecsimsw.auth.TokenCookieUtils;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class SampleController {

    private final AuthTokenService<LoginUserInfo> authTokenService;

    public SampleController(AuthTokenService<LoginUserInfo> authTokenService) {
        this.authTokenService = authTokenService;
    }

    @GetMapping("/sample/auth/needed")
    public void auth(@LoginUser LoginUserInfo info) {
        System.out.println(info.getUserId());
        System.out.println(info.getUsername());
        System.out.println(info.getUserEmail());
    }

    @PostMapping("/sample/auth/login")
    public ResponseEntity<String> login(String username, String password, HttpServletResponse response) {
        // 1. check username and password are valid with db

        // 2. make object that you want to load in auth jwt payload
        LoginUserInfo userInfo = new LoginUserInfo(username, 1L, "email");

        // 3. issue auth tokens
        AuthTokens tokens = authTokenService.issue(userInfo);

        // 4. create cookie from auth token you just created
        for(Cookie authCookie : authTokenService.createAuthCookies(tokens)) {
            response.addCookie(authCookie);
        }

        // 5. response
        return ResponseEntity.ok("login succeed");
    }
}
