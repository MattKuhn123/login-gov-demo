package kuhn.example.logingovdemo;

import java.util.Random;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.core.env.Environment;
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
    
    private final String loginGovUrl;
    private final String clientId;
    public YourUnauthenticatedRestController(Environment env) {
        loginGovUrl = env.getProperty("loginGovUrl");
        clientId = env.getProperty("clientId");
    }
    
    @GetMapping("/random")
    public String randomNumber() {
        System.out.println("enter /random (no auth)");
        System.out.println("exit /random (no auth)");
        return String.valueOf(new Random().nextInt());
    }

    @PostMapping("/login")
    public void login() {
        System.out.println("enter /login");
        System.out.println("exit /login");
    }

    @PostMapping("/logout")
    public void logout() {
        System.out.println("enter /logout");
        System.out.println("exit /logout");
    }

    @GetMapping("/redirect")
    public RedirectView redirect(@RequestParam String code, @RequestParam String state) throws Exception {
        System.out.println("enter /redirect");
        final String url = String.format("%s/api/openid_connect/token?"
                + "client_assertion=%s&"
                + "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&"
                + "code=%s&"
                + "grant_type=authorization_code", loginGovUrl, ClientJWTUtils.createClientAssertion(clientId, loginGovUrl), code);
        System.out.println(String.format("Request: [%s]", url));
        final TokenResponse response = new TokenResponse(new JSONObject(new RestTemplate().postForObject(url, null, String.class)));
        System.out.println(String.format("Result: [%s]", response.toString()));
        System.out.println("exit /redirect");
        return new RedirectView("");
    }
}
