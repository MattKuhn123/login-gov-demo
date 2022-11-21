package kuhn.example.logingovdemo;

import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

import javax.servlet.ServletException;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.FileCopyUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@Component
public class TokenService {
    
    private final String loginGovUrl;
    private final String clientId;
    private final String pemLocation;
    private final RestTemplate restTemplate;
    public TokenService(final Environment env, final RestTemplateBuilder builder) {
        loginGovUrl = env.getProperty("loginGovUrl");
        clientId = env.getProperty("clientId");
        pemLocation = env.getProperty("pemLocation");
        restTemplate = builder.build();
    }

    public TokenResponse getToken(final String code) throws ServletException {
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
