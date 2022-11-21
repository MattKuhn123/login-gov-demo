package kuhn.example.logingovdemo;

import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.FileCopyUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class Utils {
    public static final String JWT_NAME = "kuhn.example.jwt";
    public static final String STATE_NAME = "kuhn.example.state";
    public static final String NONCE_NAME = "kuhn.example.nonce";
    // TODO : Replace with your pem
    public static final String PEM_LOCATION = "classpath:security\\private-kuhn-demo.pem"; 

    /**
     * Low-level stuff. Not important for the demonstration
     * @param clientId with login.gov
     * @param pemLocation Where your pem file is.
     * @return the client assertion
     */
    public static String createClientAssertion(final String clientId, final String loginGovUrl) {
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

    private static RSAPrivateKey getPrivateKey() throws Exception {
        try {
            return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder()
                            .decode(FileCopyUtils
                                    .copyToString(new InputStreamReader(
                                            new DefaultResourceLoader().getResource(PEM_LOCATION).getInputStream()))
                                    .replace("-----BEGIN PRIVATE KEY-----", "")
                                    .replaceAll("\n", "").replace("-----END PRIVATE KEY-----", ""))));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String getCookie(final ServletRequest request, final String key) {
        for (final Cookie c : ((HttpServletRequest)request).getCookies()) {
            if (!key.equals(c.getName())) {
                continue;
            }

            return c.getValue();
        }

        throw new IllegalArgumentException(String.format("Did not find [%s] in cookies", key));
    }

    public static void setHttpCookie(final ServletResponse response, final String key, final String value) {
        final Cookie cookie = new Cookie(Utils.NONCE_NAME, "");
        cookie.setHttpOnly(true);

        if ("".equals(value)) {
            cookie.setMaxAge(0);
        }

        ((HttpServletResponse) response).addCookie(cookie);
    }
}
