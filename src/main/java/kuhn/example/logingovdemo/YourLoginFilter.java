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
public class YourLoginFilter implements Filter {

    private final String clientId;
    private final String redirectUri;

    public YourLoginFilter(final Environment env) {
        clientId = env.getProperty("clientId");
        redirectUri = env.getProperty("redirectUri");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));
        final String redirectTo = String.format("https://idp.int.identitysandbox.gov/openid_connect/authorize?"
                + "acr_values=http://idmanagement.gov/ns/assurance/ial/1&"
                + "client_id=%s&"
                + "nonce=$%s&"
                + "prompt=select_account&"
                + "redirect_uri=%s&"
                + "response_type=code&"
                + "scope=openid+email&"
                + "state=%s", clientId, java.util.UUID.randomUUID(), redirectUri, java.util.UUID.randomUUID());
        System.out.println("redirecting to: " + redirectTo);
        ((HttpServletResponse) response).setHeader("HX-Redirect", redirectTo);
        chain.doFilter(request, response);
        System.out.println(String.format("exit [%s]", getClass().getName()));
    }

    @Bean
    public FilterRegistrationBean<YourLoginFilter> loginFilter() {
        FilterRegistrationBean<YourLoginFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("/login");
        return registrationBean; 
    }
}
