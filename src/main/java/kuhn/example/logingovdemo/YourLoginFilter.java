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
import org.springframework.stereotype.Component;

@Component
public class YourLoginFilter implements Filter {
    private final String clientId = "gov:gsa:openidconnect.profiles:sp:sso:tva:kuhn_demo"; // TODO : Replace with your clientId
    private final String redirectUri = "http://localhost:8080/Redirect"; // TODO : Replace with your Redirect URI

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("enter loginFilter");
        final String redirectTo = String.format("https://idp.int.identitysandbox.gov/openid_connect/authorize?"
                + "acr_values=http://idmanagement.gov/ns/assurance/ial/1&"
                + "client_id=%s&"
                + "nonce=$%s&"
                + "prompt=select_account&"
                + "redirect_uri=%s&"
                + "response_type=code&"
                + "scope=openid+email&"
                + "state=%s", clientId, java.util.UUID.randomUUID(), redirectUri, java.util.UUID.randomUUID()).toString();
        System.out.println("redirecting to: " + redirectTo);
        ((HttpServletResponse) response).setHeader("HX-Redirect", redirectTo);
        chain.doFilter(request, response);
        System.out.println("exit loginFilter");
    }

    @Bean
    public FilterRegistrationBean<YourLoginFilter> loginFilter() {
        FilterRegistrationBean<YourLoginFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new YourLoginFilter());
        registrationBean.addUrlPatterns("/login");
        return registrationBean; 
    }
}
