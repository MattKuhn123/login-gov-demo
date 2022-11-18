package kuhn.example.logingovdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoginGovDemoApplication {

	public static final String PEM_LOCATION = "classpath:security\\private-kuhn-demo.pem"; // TODO : Replace with your pem

	public static void main(String[] args) {
		SpringApplication.run(LoginGovDemoApplication.class, args);
	}
}
