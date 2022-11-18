package kuhn.example.logingovdemo;

import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.FileCopyUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class Utils {
    /**
     * Lots of low-level stuff. Not important for the demonstration
     * @param clientId with login.gov
     * @param expirationTime how long until your token expires
     * @param pemLocation Where your pem file is.
     * @return the client assertion
     */
    public static String createClientAssertion(final String clientId, final int expirationTime, final String pemLocation) {
        try {
            return JWT.create()
                    .withIssuer(clientId)
                    .withSubject(clientId)
                    .withAudience("https://idp.int.identitysandbox.gov/api/openid_connect/token")
                    .withJWTId(java.util.UUID.randomUUID().toString())
                    .withExpiresAt(Instant.ofEpochMilli(Instant.now().toEpochMilli() + expirationTime))
                    .sign(Algorithm.RSA256(getPrivateKey(pemLocation)));
        } catch (Exception e) {
            System.out.println("Something went wrong generating the client assertion");
            e.printStackTrace();
            return "";
        }
    }

    public static RSAPrivateKey getPrivateKey(final String pemLocation) throws Exception {
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