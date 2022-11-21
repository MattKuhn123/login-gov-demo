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
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));
        final String code = req.getParameter("code");
        final String state = req.getParameter("state");
        System.out.println(String.format("Redirected with code [%s], state [%s]", code, state));

        if (!state.equals(UtilsCookies.getHttpCookie(req, UtilsCookies.STATE_NAME))) {
            System.out.println("State invalid");
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "State invalid");
            return;
        } else {
            System.out.println("State valid");
        }

        final TokenResponse jwtResponse = tokenService.getToken(code);
        if (!jwtResponse.getNonce().equals(UtilsCookies.getHttpCookie(req, UtilsCookies.NONCE_NAME))) {
            System.out.println("Nonce invalid");
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Nonce invalid");
            return;
        } else {
            System.out.println("Nonce valid");
        }

        UtilsCookies.setHttpCookie(res, UtilsCookies.JWT_NAME, jwtResponse.getEncodedIdToken());

        chain.doFilter(req, res);
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
