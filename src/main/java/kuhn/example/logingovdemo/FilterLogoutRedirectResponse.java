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

public class FilterLogoutRedirectResponse implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));

        final String state = request.getParameter("state");
        if (!state.equals(Utils.getCookie(request, Utils.STATE_NAME))) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "State invalid.");
            return;
        }
        
        Utils.setHttpCookie(response, Utils.JWT_NAME, "");
        Utils.setHttpCookie(response, Utils.STATE_NAME, "");
        Utils.setHttpCookie(response, Utils.NONCE_NAME, "");

        chain.doFilter(request, response);

        System.out.println(String.format("exit [%s]", getClass().getName()));
    }
    
    @Bean
    public FilterRegistrationBean<FilterLogoutRedirectResponse> redirectFilter() {
        FilterRegistrationBean<FilterLogoutRedirectResponse> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("/redirectLogout");
        return registrationBean; 
    }
}
