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
public class FilterLogout implements Filter {
    
    private final String clientId;
    private final String redirectUri;
    private final String loginGovUrl;
    public FilterLogout(final Environment env) {
        clientId = env.getProperty("clientId");
        redirectUri = env.getProperty("logoutRedirectUri");
        loginGovUrl = env.getProperty("loginGovUrl");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));

        final UUID state = java.util.UUID.randomUUID();
        final Cookie stateCookie = new Cookie(Utils.STATE_NAME, state.toString());
        stateCookie.setHttpOnly(true);
        ((HttpServletResponse) response).addCookie(stateCookie);

        final String redirectTo = String.format("%sopenid_connect/logout?"
                + "client_id=%s&"
                + "post_logout_redirect_uri=%s&"
                + "state=%s", loginGovUrl, clientId, redirectUri, state);
        System.out.println("redirecting to: " + redirectTo);
        ((HttpServletResponse) response).setHeader("HX-Redirect", redirectTo);
        chain.doFilter(request, response);

        final Cookie jwtCookie = new Cookie(Utils.JWT_NAME, "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(0);
        ((HttpServletResponse) response).addCookie(jwtCookie);

        final Cookie nonceCookies = new Cookie(Utils.NONCE_NAME, "");
        nonceCookies.setHttpOnly(true);
        nonceCookies.setMaxAge(0);
        ((HttpServletResponse) response).addCookie(nonceCookies);
        
        System.out.println(String.format("exit [%s]", getClass().getName()));
    }

    @Bean
    public FilterRegistrationBean<FilterLogout> logoutFilter() {
        FilterRegistrationBean<FilterLogout> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("/logout");
        return registrationBean; 
    }
}
