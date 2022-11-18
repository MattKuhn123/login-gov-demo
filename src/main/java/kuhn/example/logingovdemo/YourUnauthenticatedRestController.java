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
    public RedirectView redirect(@RequestParam String code, @RequestParam String state) throws Exception {
        System.out.println("Enter redirect endpoint");
        final String url = String.format("%s/api/openid_connect/token?"
                + "client_assertion=%s&"
                + "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&"
                + "code=%s&"
                + "grant_type=authorization_code", loginGovUrl, ClientJWTUtils.createClientAssertion(clientId, loginGovUrl), code);
        System.out.println(String.format("Request: [%s]", url));
        final TokenResponse response = new TokenResponse(new JSONObject(new RestTemplate().postForObject(url, null, String.class)));
        System.out.println(String.format("Result: [%s]", response.toString()));
        System.out.println("Exit redirect endpoint");
        return new RedirectView("");
    }
}
