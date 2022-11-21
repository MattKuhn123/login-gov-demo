package kuhn.example.logingovdemo;

import java.io.IOException;

import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

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

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.FileCopyUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@Component
public class FilterLoginRedirectResponse implements Filter {

    private final String loginGovUrl;
    private final String clientId;
    private final String pemLocation;
    private final RestTemplate restTemplate;
    public FilterLoginRedirectResponse(final Environment env, final RestTemplateBuilder builder) {
        loginGovUrl = env.getProperty("loginGovUrl");
        clientId = env.getProperty("clientId");
        pemLocation = env.getProperty("pemLocation");
        restTemplate = builder.build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println(String.format("enter [%s]", getClass().getName()));
        final String code = request.getParameter("code");
        final String state = request.getParameter("state");
        System.out.println(String.format("Redirected with code [%s], state [%s]", code, state));

        if (!state.equals(Utils.getHttpCookie(request, Utils.STATE_NAME))) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "State invalid.");
            return;
        }

        final TokenResponse jwtResponse = getToken(code);
        if (!loginGovUrl.equals(jwtResponse.getIssuer())) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Issuer invalid.");
            return;
        }

        if (!jwtResponse.getNonce().equals(Utils.getHttpCookie(request, Utils.NONCE_NAME))) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Nonce invalid.");
            return;
        }

        Utils.setHttpCookie(response, Utils.JWT_NAME, jwtResponse.getEncodedIdToken());

        chain.doFilter(request, response);
        System.out.println(String.format("exit [%s]", getClass().getName()));
    }

    @Bean
    public FilterRegistrationBean<FilterLoginRedirectResponse> redirectFilter() {
        FilterRegistrationBean<FilterLoginRedirectResponse> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("/redirectLogin");
        return registrationBean; 
    }

    private TokenResponse getToken(final String code) throws ServletException {
        final String url = String.format("%sapi/openid_connect/token?"
                + "client_assertion=%s&"
                + "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&"
                + "code=%s&"
                + "grant_type=authorization_code", loginGovUrl, createClientAssertion(clientId, loginGovUrl), code);

        System.out.println(String.format("Request: [%s]", url));
        final TokenResponse jwtResponse = restTemplate.postForObject(url, null, TokenResponse.class);
        if (jwtResponse == null) {
            throw new ServletException("Failed to get token");
        }

        System.out.println(String.format("Result: [%s]", jwtResponse.toString()));
        return jwtResponse;
    }

    private String createClientAssertion(final String clientId, final String loginGovUrl) {
        try {
            return JWT.create()
                    .withIssuer(clientId)
                    .withSubject(clientId)
                    .withAudience(loginGovUrl + "api/openid_connect/token")
                    .withJWTId(java.util.UUID.randomUUID().toString())
                    .withExpiresAt(Instant.ofEpochMilli(Instant.now().toEpochMilli() + 100000))
                    .sign(Algorithm.RSA256(getPrivateKey()));
        } catch (Exception e) {
            System.out.println("Something went wrong generating the client assertion");
            e.printStackTrace();
            return "";
        }
    }

    private RSAPrivateKey getPrivateKey() throws Exception {
        try {
            return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder()
                            .decode(FileCopyUtils
                                    .copyToString(new InputStreamReader(
                                            new DefaultResourceLoader().getResource(pemLocation).getInputStream()))
                                    .replace("-----BEGIN PRIVATE KEY-----", "")
                                    .replaceAll("\n", "").replace("-----END PRIVATE KEY-----", ""))));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
