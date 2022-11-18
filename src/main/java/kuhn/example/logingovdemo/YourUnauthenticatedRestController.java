package kuhn.example.logingovdemo;

import java.util.Random;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/")
public class YourUnauthenticatedRestController {
    private final String clientId = "gov:gsa:openidconnect.profiles:sp:sso:tva:kuhn_demo"; // TODO : Replace with your clientId
    private final String pemLocation = "classpath:security\\private-kuhn-demo.pem"; // TODO : Replace with your pem
    
    @GetMapping("/random")
    public String randomNumber() {
        System.out.println("In unauthenticated endpoint");
        return String.valueOf(new Random().nextInt());
    }

    @PostMapping("/login")
    public void login() {
        System.out.println("login endpoint");
    }

    @PostMapping("/logout")
    public void logout() {
        System.out.println("logout endpoint");
    }

    @GetMapping("/Redirect")
    public RedirectView redirect(@RequestParam String code, @RequestParam String state) {
        System.out.println("login.gov has redirected back to us with an authorization code: " + code);
        final String url = String.format("https://idp.int.identitysandbox.gov/api/openid_connect/token?"
                + "client_assertion=%s&"
                + "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&"
                + "code=%s&"
                + "grant_type=authorization_code", Utils.createClientAssertion(clientId, pemLocation), code);
        System.out.println("Requesting back to login.gov with the authorization code for a jwt token: " + System.lineSeparator() + url);
        final String response = new RestTemplate().postForObject(url, null, String.class);
        System.out.println("Result from request to to login.gov for a jwt token: " + System.lineSeparator() + response);
        return new RedirectView("");
    }
}
