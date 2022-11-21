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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));

        final String nonce = Utils.getHttpCookie(request, Utils.NONCE_NAME);
        final DecodedJWT decodedJWT = JWT.decode(Utils.getHttpCookie(request, Utils.JWT_NAME));
        final boolean iss = decodedJWT.getIssuer().equals(loginGovUrl);
        final boolean aud = decodedJWT.getClaim("aud").asString().equals(clientId);
        final boolean expired = new Date(Instant.now().toEpochMilli()).compareTo(decodedJWT.getExpiresAt()) > 0;
        final boolean non = decodedJWT.getClaim("nonce").asString().substring(1).equals(nonce);
        
        if (iss && aud && !expired && non) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalid");
            return;
        }

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