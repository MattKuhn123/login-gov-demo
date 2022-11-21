package kuhn.example.logingovdemo;

import java.util.Random;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/")
public class ControllerUnauth {
    
    @GetMapping("/random")
    public String randomNumber() {
        System.out.println("enter [/random (no auth)]");
        System.out.println("exit [/random (no auth)]");
        return String.valueOf(new Random().nextInt());
    }

    @PostMapping("/login")
    public void login() {
        System.out.println("enter [/login]");
        System.out.println("exit [/login]");
    }

    @PostMapping("/logout")
    public void logout() {
        System.out.println("enter [/logout]");
        System.out.println("exit [/logout]");
    }

    @GetMapping("/redirect")
    public RedirectView redirect(@RequestParam String code, @RequestParam String state) throws Exception {
        System.out.println("enter [/redirect]");
        System.out.println("exit [/redirect]");
        return new RedirectView("");
    }
}
