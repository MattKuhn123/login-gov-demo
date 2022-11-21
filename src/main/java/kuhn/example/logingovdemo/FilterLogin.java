package kuhn.example.logingovdemo;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class FilterLogin implements Filter {

    private final String clientId;
    private final String loginRedirectUri;
    private final String loginGovUrl;
    public FilterLogin(final Environment env) {
        clientId = env.getProperty("clientId");
        loginRedirectUri = env.getProperty("loginRedirectUri");
        loginGovUrl = env.getProperty("loginGovUrl");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));

        final UUID nonce = java.util.UUID.randomUUID();
        UtilsCookies.setHttpCookie(res, UtilsCookies.NONCE_NAME, nonce.toString());
        
        final UUID state = java.util.UUID.randomUUID();
        UtilsCookies.setHttpCookie(res, UtilsCookies.STATE_NAME, state.toString());

        final String redirectTo = String.format("%s/openid_connect/authorize?"
                + "acr_values=http://idmanagement.gov/ns/assurance/ial/1&"
                + "client_id=%s&"
                + "nonce=$%s&"
                + "prompt=select_account&"
                + "redirect_uri=%s&"
                + "response_type=code&"
                + "scope=openid+email&"
                + "state=%s", loginGovUrl, clientId, nonce, loginRedirectUri, state);
        System.out.println("redirecting to: " + redirectTo);
        ((HttpServletResponse) res).setHeader("HX-Redirect", redirectTo);
        chain.doFilter(req, res);
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
