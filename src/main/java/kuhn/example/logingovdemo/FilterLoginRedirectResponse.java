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
public class FilterLoginRedirectResponse implements Filter {

    private final TokenService tokenService;
    public FilterLoginRedirectResponse(final TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));
        final String code = request.getParameter("code");
        final String state = request.getParameter("state");
        System.out.println(String.format("Redirected with code [%s], state [%s]", code, state));

        if (!state.equals(UtilsCookies.getHttpCookie(request, UtilsCookies.STATE_NAME))) {
            System.out.println("State invalid");
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "State invalid");
            return;
        } else {
            System.out.println("State valid");
        }

        final TokenResponse jwtResponse = tokenService.getToken(code);
        if (!jwtResponse.getNonce().equals(UtilsCookies.getHttpCookie(request, UtilsCookies.NONCE_NAME))) {
            System.out.println("Nonce invalid");
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Nonce invalid");
            return;
        } else {
            System.out.println("Nonce valid");
        }

        UtilsCookies.setHttpCookie(response, UtilsCookies.JWT_NAME, jwtResponse.getEncodedIdToken());

        chain.doFilter(request, response);
        System.out.println(String.format("exit [%s]", getClass().getName()));
    }

    @Bean
    public FilterRegistrationBean<FilterLoginRedirectResponse> redirectLoginFilter() {
        FilterRegistrationBean<FilterLoginRedirectResponse> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("/redirectLogin");
        return registrationBean; 
    }

}
