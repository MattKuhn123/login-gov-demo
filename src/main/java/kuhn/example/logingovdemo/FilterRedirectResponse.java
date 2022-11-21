package kuhn.example.logingovdemo;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FilterRedirectResponse implements Filter {

    private final String loginGovUrl;
    private final String clientId;
    private final RestTemplate restTemplate;
    public FilterRedirectResponse(final Environment env, final RestTemplateBuilder builder) {
        loginGovUrl = env.getProperty("loginGovUrl");
        clientId = env.getProperty("clientId");
        restTemplate = builder.build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));
        final String code = request.getParameter("code");
        final String state = request.getParameter("state");

        if (!state.equals(Utils.getCookie(request, Utils.STATE_NAME))) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "State invalid.");
            return;
        }

        final TokenResponse jwtResponse = getToken(code);
        if (!loginGovUrl.equals(jwtResponse.getIssuer())) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Issuer invalid.");
            return;
        }

        if (!jwtResponse.getNonce().equals(Utils.getCookie(request, Utils.NONCE_NAME))) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Nonce invalid.");
            return;
        }

        Utils.setHttpCookie(response, Utils.JWT_NAME, jwtResponse.getEncodedIdToken());

        chain.doFilter(request, response);
        System.out.println(String.format("exit [%s]", getClass().getName()));
    }

    @Bean
    public FilterRegistrationBean<FilterRedirectResponse> redirectFilter() {
        FilterRegistrationBean<FilterRedirectResponse> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("/redirect");
        return registrationBean; 
    }

    private TokenResponse getToken(final String code) throws ServletException {
        final String url = String.format("%sapi/openid_connect/token?"
                + "client_assertion=%s&"
                + "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&"
                + "code=%s&"
                + "grant_type=authorization_code", loginGovUrl, Utils.createClientAssertion(clientId, loginGovUrl), code);

        System.out.println(String.format("Request: [%s]", url));
        final TokenResponse jwtResponse = restTemplate.postForObject(url, null, TokenResponse.class);
        if (jwtResponse == null) {
            throw new ServletException("Failed to get token");
        }

        System.out.println(String.format("Result: [%s]", jwtResponse.toString()));
        return jwtResponse;
    }
}
