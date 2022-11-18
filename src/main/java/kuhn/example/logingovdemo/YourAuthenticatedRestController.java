package kuhn.example.logingovdemo;

import java.util.Random;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class YourAuthenticatedRestController {
    
    @GetMapping("/random")
    public String randomNumber() {
        System.out.println("enter [/auth/random]");
        System.out.println("exit [/auth/random]");
        return "(auth) " + String.valueOf(new Random().nextInt());
    }
}
