package kuhn.example.logingovdemo;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class YourJwtAuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("entering chain!");
        final String val = ((HttpServletRequest) request).getHeader("token");
        if (val == null || val == "") {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "The token is not valid.");
        } else {
            chain.doFilter(request, response);
        }

        System.out.println("exiting chain!");
    }

    @Bean
    public FilterRegistrationBean<YourJwtAuthenticationFilter> jwtFilter() {
        FilterRegistrationBean<YourJwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new YourJwtAuthenticationFilter());
        registrationBean.addUrlPatterns("/auth/*");
        return registrationBean; 
    }
}