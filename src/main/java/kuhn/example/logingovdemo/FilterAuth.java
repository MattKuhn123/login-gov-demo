package kuhn.example.logingovdemo;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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

        boolean iss = false;
        boolean aud = false;
        boolean expired = true;
        boolean non = false;

        final String nonce = Utils.getCookie(request, Utils.NONCE_NAME);

        for(final Cookie c : ((HttpServletRequest) request).getCookies()) {
            if (!Utils.JWT_NAME.equals(c.getName())) {
                continue;
            }

            try {
                final DecodedJWT decodedJWT = JWT.decode(c.getValue());
                iss = decodedJWT.getIssuer().equals(loginGovUrl);
                aud = decodedJWT.getClaim("aud").asString().equals(clientId);
                expired = new Date(Instant.now().toEpochMilli()).compareTo(decodedJWT.getExpiresAt()) > 0;
                non = decodedJWT.getClaim("nonce").asString().substring(1).equals(nonce);
            } catch (final Exception e) { }
        }
        
        if (iss && aud && !expired && non) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalid.");
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