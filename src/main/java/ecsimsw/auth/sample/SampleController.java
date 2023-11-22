package ecsimsw.auth.sample;

import ecsimsw.auth.anotations.JwtPayload;
import ecsimsw.auth.service.AuthTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class SampleController {

    private final AuthTokenService authTokenService;

    public SampleController(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @PostMapping("/sample/auth")
    public ResponseEntity<String> login(String username, String password, HttpServletResponse response) {
        // 1. check username and password are valid with db

        // 2. make object that you want to load in auth jwt payload
        MyLoginPayload userInfo = new MyLoginPayload("ecsimsw", "ecsimsw@gmail.com");

        // 3. issue auth tokens
        authTokenService.issue(response, userInfo);

        // 4. response
        return ResponseEntity.ok("login succeed");
    }

    @GetMapping("/sample/auth")
    public ResponseEntity<String> auth(@JwtPayload MyLoginPayload info) {
        return ResponseEntity.ok(info.getName() + " " + info.getEmail());
    }
}
