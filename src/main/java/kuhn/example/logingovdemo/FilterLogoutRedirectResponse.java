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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));

        final String state = request.getParameter("state");
        if (!state.equals(UtilsCookies.getHttpCookie(request, UtilsCookies.STATE_NAME))) {
            System.out.println("State invalid");
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "State invalid");
            return;
        }
        
        UtilsCookies.setHttpCookie(response, UtilsCookies.JWT_NAME, "");
        UtilsCookies.setHttpCookie(response, UtilsCookies.STATE_NAME, "");
        UtilsCookies.setHttpCookie(response, UtilsCookies.NONCE_NAME, "");

        chain.doFilter(request, response);

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
