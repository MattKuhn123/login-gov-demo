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

    @GetMapping("/redirectLogin")
    public RedirectView redirectLogin(@RequestParam String code, @RequestParam String state) {
        System.out.println("enter [/redirectLogin]");
        System.out.println("exit [/redirectLogin]");
        return new RedirectView("");
    }

    @GetMapping("/redirectLogout")
    public RedirectView redirectLogout(@RequestParam String state) {
        System.out.println("enter [/redirectLogout]");
        System.out.println("exit [/redirectLogout]");
        return new RedirectView("");
    }
}
