package kuhn.example.logingovdemo;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

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

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class FilterAuth implements Filter {

    private final String clientId;
    private final String loginGovUrl;
    public FilterAuth(final Environment env) {
        clientId = env.getProperty("clientId");
        loginGovUrl = env.getProperty("loginGovUrl");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));

        final DecodedJWT decodedJWT = JWT.decode(UtilsCookies.getHttpCookie(req, UtilsCookies.JWT_NAME));
        if (!decodedJWT.getIssuer().equals(loginGovUrl)) {
            System.out.println("Invalid issuer");
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid issuer");
            return;
        }

        if (!decodedJWT.getClaim("aud").asString().equals(clientId)) {
            System.out.println("Invalid audience");
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid audience");
            return;
        }

        if (new Date(Instant.now().toEpochMilli()).after((decodedJWT.getExpiresAt()))) {
            System.out.println("Token expired");
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return;
        }

        if (!decodedJWT.getClaim("nonce").asString().substring(1).equals(UtilsCookies.getHttpCookie(req, UtilsCookies.NONCE_NAME))) {
            System.out.println("Invalid nonce");
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid nonce");
            return;
        }

        chain.doFilter(req, res);

        System.out.println(String.format("exit [%s]", getClass().getName()));
    }

    @Bean
    public FilterRegistrationBean<FilterAuth> jwtFilter() {
        FilterRegistrationBean<FilterAuth> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("/auth/*");
        return registrationBean; 
    }
}