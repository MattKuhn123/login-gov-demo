package kuhn.example.logingovdemo;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FilterRedirect implements Filter {

    private final String loginGovUrl;
    private final String clientId;
    public FilterRedirect(Environment env) {
        loginGovUrl = env.getProperty("loginGovUrl");
        clientId = env.getProperty("clientId");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));
        final String code = request.getParameter("code");
        final String state = request.getParameter("state");
        final String url = String.format("%sapi/openid_connect/token?"
                + "client_assertion=%s&"
                + "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&"
                + "code=%s&"
                + "grant_type=authorization_code", loginGovUrl, UtilsJwt.createClientAssertion(clientId, loginGovUrl), code);
        try {
            System.out.println(String.format("Request: [%s]", url));
            final TokenResponse jwtResponse = new TokenResponse(new JSONObject(new RestTemplate().postForObject(url, null, String.class)));
            System.out.println(String.format("Result: [%s]", jwtResponse.toString()));
            if (!loginGovUrl.equals(jwtResponse.getIssuer())) {
                throw new Exception();
            }

            Cookie cookie = new Cookie(UtilsJwt.JWT_NAME, jwtResponse.getEncodedIdToken());
            cookie.setHttpOnly(true);
            ((HttpServletResponse) response).addCookie(cookie);

            chain.doFilter(request, response);
        } catch (final Exception e) {
            e.printStackTrace();
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "The token is not valid.");
            return;
        }

        System.out.println(String.format("exit [%s]", getClass().getName()));
    }

    @Bean
    public FilterRegistrationBean<FilterRedirect> redirectFilter() {
        FilterRegistrationBean<FilterRedirect> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("/redirect");
        return registrationBean; 
    }
}
