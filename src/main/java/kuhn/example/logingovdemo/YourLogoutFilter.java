package kuhn.example.logingovdemo;

import java.io.IOException;

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
public class YourLogoutFilter implements Filter {
    
    private final String clientId;
    private final String redirectUri;
    private final String loginGovUrl;
    public YourLogoutFilter(final Environment env) {
        clientId = env.getProperty("clientId");
        redirectUri = env.getProperty("logoutRedirectUri");
        loginGovUrl = env.getProperty("loginGovUrl");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));
        final String redirectTo = String.format("%s/openid_connect/logout?"
                + "client_id=%s&"
                + "post_logout_redirect_uri=%s&"
                + "state=%s", loginGovUrl, clientId, redirectUri, java.util.UUID.randomUUID());
        System.out.println("redirecting to: " + redirectTo);
        ((HttpServletResponse) response).setHeader("HX-Redirect", redirectTo);
        chain.doFilter(request, response);
        System.out.println(String.format("exit [%s]", getClass().getName()));
    }

    @Bean
    public FilterRegistrationBean<YourLogoutFilter> logoutFilter() {
        FilterRegistrationBean<YourLogoutFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("/logout");
        return registrationBean; 
    }
}
