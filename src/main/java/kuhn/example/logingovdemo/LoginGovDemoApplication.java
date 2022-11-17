package kuhn.example.logingovdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class LoginGovDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoginGovDemoApplication.class, args);
	}
}
