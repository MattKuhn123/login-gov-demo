package kuhn.example.logingovdemo;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class FilterLogin implements Filter {

    private final String clientId;
    private final String redirectUri;
    private final String loginGovUrl;
    public FilterLogin(final Environment env) {
        clientId = env.getProperty("clientId");
        redirectUri = env.getProperty("redirectUri");
        loginGovUrl = env.getProperty("loginGovUrl");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));

        final UUID nonce = java.util.UUID.randomUUID();
        final UUID state = java.util.UUID.randomUUID();

        final Cookie nonceCookie = new Cookie(Utils.NONCE_NAME, nonce.toString());
        nonceCookie.setHttpOnly(true);
        ((HttpServletResponse) response).addCookie(nonceCookie);

        final Cookie stateCookie = new Cookie(Utils.STATE_NAME, state.toString());
        stateCookie.setHttpOnly(true);
        ((HttpServletResponse) response).addCookie(stateCookie);

        final String redirectTo = String.format("%s/openid_connect/authorize?"
                + "acr_values=http://idmanagement.gov/ns/assurance/ial/1&"
                + "client_id=%s&"
                + "nonce=$%s&"
                + "prompt=select_account&"
                + "redirect_uri=%s&"
                + "response_type=code&"
                + "scope=openid+email&"
                + "state=%s", loginGovUrl, clientId, nonce, redirectUri, state);
        System.out.println("redirecting to: " + redirectTo);
        ((HttpServletResponse) response).setHeader("HX-Redirect", redirectTo);
        chain.doFilter(request, response);
        System.out.println(String.format("exit [%s]", getClass().getName()));
    }

    @Bean
    public FilterRegistrationBean<FilterLogin> loginFilter() {
        FilterRegistrationBean<FilterLogin> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("/login");
        return registrationBean; 
    }
}
