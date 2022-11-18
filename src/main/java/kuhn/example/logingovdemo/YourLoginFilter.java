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

public class YourLoginFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("entering chain!");
        final String val = ((HttpServletRequest) request).getHeader("token");
        if (val == null || val == "") {
            ((HttpServletResponse) response).setHeader("HX-Redirect", "https://www.yahoo.com");
        } else {
            chain.doFilter(request, response);
        }

        System.out.println("exiting chain!");
    }

    @Bean
    public FilterRegistrationBean<YourLoginFilter> jwtFilter() {
        FilterRegistrationBean<YourLoginFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new YourLoginFilter());
        registrationBean.addUrlPatterns("/login");
        return registrationBean; 
    }
}
