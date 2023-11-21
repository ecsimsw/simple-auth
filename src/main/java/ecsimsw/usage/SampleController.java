package ecsimsw.usage;

import ecsimsw.auth.LoginUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @GetMapping("/sample/auth/needed")
    public void auth(@LoginUser LoginUserInfo info) {
        System.out.println(info.getUserId());
        System.out.println(info.getUsername());
        System.out.println(info.getUserEmail());
    }
}
