package kuhn.example.logingovdemo;

import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class ControllerAuth {

    private final UserInfoService userInfoService;
    public ControllerAuth(final UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }
    
    @GetMapping("/random")
    public String randomNumber() {
        System.out.println("enter [/auth/random]");
        System.out.println("exit [/auth/random]");
        return String.format("(auth) %s", String.valueOf(new Random().nextInt()));
    }

    @GetMapping("/email")
    public String email(final ServletRequest req) {
        try {
            System.out.println("enter [/auth/email]");
            final UserInfoResponse userInfoResponse = userInfoService.getUserInfo(req);
            System.out.println("exit [/auth/email]");
            return userInfoResponse.getEmail();
        } catch (final ServletException e) {
            System.out.println("error in [/auth/email]");
            e.printStackTrace();
            return "";
        }
    }
}
