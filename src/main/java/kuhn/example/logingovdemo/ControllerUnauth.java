package kuhn.example.logingovdemo;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.thymeleaf.util.StringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

@RestController
@RequestMapping("/")
public class ControllerUnauth {

    private final TokenService tokenService;
    private final String clientId;
    private final String loginRedirectUri;
    private final String logoutRedirectUri;
    private final String loginGovUrl;
    public ControllerUnauth(final Environment env, final TokenService tokenService) {
        clientId = env.getProperty("clientId");
        loginGovUrl = env.getProperty("loginGovUrl");
        loginRedirectUri = env.getProperty("loginRedirectUri");
        logoutRedirectUri = env.getProperty("logoutRedirectUri");
        this.tokenService = tokenService;
    }
    
    @GetMapping("/")
    public ModelAndView index(final ServletRequest req) {
        final ModelAndView m = new ModelAndView();
        m.setViewName("index");
        try {
            final DecodedJWT decodedJWT = JWT.decode(CookieUtils.getCookie(req, CookieUtils.JWT_NAME));
            m.addObject("authenticated", new Date(Instant.now().toEpochMilli()).before((decodedJWT.getExpiresAt())));
        } catch (final Exception e) {
            m.addObject("authenticated", false);
        }

        return m;
    }

    @GetMapping("/random")
    public String randomNumber() {
        System.out.println("enter [/random (no auth)]");
        System.out.println("exit [/random (no auth)]");
        return String.valueOf(new Random().nextInt());
    }

    @PostMapping("/login")
    public void login(final ServletRequest req, final ServletResponse res) {
        System.out.println("enter [/login]");

        final UUID nonce = java.util.UUID.randomUUID();
        CookieUtils.setHttpCookie(res, CookieUtils.NONCE_NAME, nonce.toString(), CookieUtils.FIFTEEN_MINUTES);
        
        final UUID state = java.util.UUID.randomUUID();
        CookieUtils.setHttpCookie(res, CookieUtils.STATE_NAME, state.toString(), CookieUtils.FIFTEEN_MINUTES);

        final String forwardToLogingov = String.format("%s/openid_connect/authorize?"
                + "acr_values=http://idmanagement.gov/ns/assurance/ial/1&"
                + "client_id=%s&"
                + "nonce=$%s&"
                + "prompt=select_account&"
                + "redirect_uri=%s&"
                + "response_type=code&"
                + "scope=openid+email&"
                + "state=%s", loginGovUrl, clientId, nonce, loginRedirectUri, state);
        System.out.println("forwarding to: " + forwardToLogingov);
        // forward user to logingov
        ((HttpServletResponse) res).setHeader("HX-Redirect", forwardToLogingov);

        System.out.println("exit [/login]");
    }

    @PostMapping("/logout")
    public void logout(final ServletRequest req, final ServletResponse res) {
        System.out.println("enter [/logout]");

        final UUID state = java.util.UUID.randomUUID();
        CookieUtils.setHttpCookie(res, CookieUtils.STATE_NAME, state.toString(), CookieUtils.FIFTEEN_MINUTES);

        final String redirectTo = String.format("%sopenid_connect/logout?"
                + "client_id=%s&"
                + "post_logout_redirect_uri=%s&"
                + "state=%s", loginGovUrl, clientId, logoutRedirectUri, state);
        System.out.println("redirecting to: " + redirectTo);
        ((HttpServletResponse) res).setHeader("HX-Redirect", redirectTo);

        System.out.println("exit [/logout]");
    }

    @GetMapping("/redirectLogin")
    public RedirectView redirectLogin(final ServletRequest req, final ServletResponse res, @RequestParam String code, @RequestParam String state, @RequestParam(defaultValue = "", required = false) String error) 
            throws ServletException, IOException {
        System.out.println(String.format("enter [/redirectLogin], code: [%s], state: [%s], error: [%s]", code, state, error));
        if (!StringUtils.isEmptyOrWhitespace(error)) {
            return new RedirectView("error");
        }

        if (!state.equals(CookieUtils.getCookie(req, CookieUtils.STATE_NAME))) {
            System.out.println("State invalid");
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "State invalid");
            return new RedirectView("");
        } else {
            System.out.println("State valid");
        }

        // use code to request jwt token
        final TokenResponse jwtResponse = tokenService.getToken(code);
        
        // validate jwt token
        if (!jwtResponse.getNonce().equals(CookieUtils.getCookie(req, CookieUtils.NONCE_NAME))) {
            System.out.println("Nonce invalid");
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Nonce invalid");
            return new RedirectView("");
        } else {
            System.out.println("Nonce valid");
        }

        CookieUtils.setHttpCookie(res, CookieUtils.JWT_NAME, jwtResponse.getEncodedIdToken(), jwtResponse.getExpiresIn());
        CookieUtils.setHttpCookie(res, CookieUtils.ACCESS_NAME, jwtResponse.getAccessToken(), jwtResponse.getExpiresIn());

        System.out.println("exit [/redirectLogin]");
        return new RedirectView("");
    }

    @GetMapping("/redirectLogout")
    public RedirectView redirectLogout(final ServletRequest req, final ServletResponse res, @RequestParam String state)
            throws ServletException, IOException {
        System.out.println(String.format("enter [/redirectLogout], state: [%s]", state));
        if (!state.equals(CookieUtils.getCookie(req, CookieUtils.STATE_NAME))) {
            System.out.println("State invalid");
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "State invalid");
            return new RedirectView("");
        } else {
            System.out.println("State valid");
        }
        
        CookieUtils.deleteCookie(res, CookieUtils.JWT_NAME);
        CookieUtils.deleteCookie(res, CookieUtils.ACCESS_NAME);
        CookieUtils.deleteCookie(res, CookieUtils.STATE_NAME);
        CookieUtils.deleteCookie(res, CookieUtils.NONCE_NAME);

        System.out.println("exit [/redirectLogout]");
        return new RedirectView("");
    }
}
