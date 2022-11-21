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
public class FilterLogout implements Filter {
    
    private final String clientId;
    private final String logoutRedirectUri;
    private final String loginGovUrl;
    public FilterLogout(final Environment env) {
        clientId = env.getProperty("clientId");
        logoutRedirectUri = env.getProperty("logoutRedirectUri");
        loginGovUrl = env.getProperty("loginGovUrl");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));

        final UUID state = java.util.UUID.randomUUID();
        UtilsCookies.setHttpCookie(response, UtilsCookies.STATE_NAME, state.toString());

        final String redirectTo = String.format("%sopenid_connect/logout?"
                + "client_id=%s&"
                + "post_logout_redirect_uri=%s&"
                + "state=%s", loginGovUrl, clientId, logoutRedirectUri, state);
        System.out.println("redirecting to: " + redirectTo);
        ((HttpServletResponse) response).setHeader("HX-Redirect", redirectTo);
        chain.doFilter(request, response);
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
