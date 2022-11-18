package kuhn.example.logingovdemo;

import java.util.Random;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class YourUnauthenticatedRestController {

    @GetMapping("/random")
    public String randomNumber() {
        System.out.println("In unauthenticated endpoint");
        return String.valueOf(new Random().nextInt());
    }

    @PostMapping("/login")
    public void login() {
        System.out.println("In login");
        
    }
}
