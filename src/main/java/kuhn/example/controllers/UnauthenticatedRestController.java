package kuhn.example.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UnauthenticatedRestController {
    
    @GetMapping("/hi")
    public String Hi() {
        return "hello";
    }
}
