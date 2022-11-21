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
public class FilterLogoutRedirectResponse implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));

        final String state = req.getParameter("state");
        if (!state.equals(UtilsCookies.getHttpCookie(req, UtilsCookies.STATE_NAME))) {
            System.out.println("State invalid");
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "State invalid");
            return;
        }
        
        UtilsCookies.setHttpCookie(res, UtilsCookies.JWT_NAME, "");
        UtilsCookies.setHttpCookie(res, UtilsCookies.STATE_NAME, "");
        UtilsCookies.setHttpCookie(res, UtilsCookies.NONCE_NAME, "");

        chain.doFilter(req, res);

        System.out.println(String.format("exit [%s]", getClass().getName()));
    }
    
    @Bean
    public FilterRegistrationBean<FilterLogoutRedirectResponse> redirectLogoutFilter() {
        FilterRegistrationBean<FilterLogoutRedirectResponse> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("/redirectLogout");
        return registrationBean; 
    }
}
