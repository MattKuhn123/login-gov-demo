package kuhn.example.logingovdemo;

import org.codehaus.jettison.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

public class TokenResponse {

    private final String accessToken;
    private final String tokenType;
    private final int expiresIn;
    private final String encodedIdToken;
    private final String issuer;

    public TokenResponse(final JSONObject jsonObject) throws Exception {
        accessToken = (String) jsonObject.get("access_token");
        tokenType = (String) jsonObject.get("token_type");
        expiresIn = (int) jsonObject.get("expires_in");
        encodedIdToken = (String) jsonObject.get("id_token");
        final DecodedJWT decodedJWT = JWT.decode(encodedIdToken);
        issuer = decodedJWT.getIssuer();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getEncodedIdToken() {
        return encodedIdToken;
    }

    public String getIssuer() {
        return issuer;
    }

    @Override
    public String toString() {
        return String.format("access_token: [%s] token_type: [%s], expires_in: [%s], encodedIdToken: [%s], iss: [%s]", accessToken, tokenType, expiresIn, encodedIdToken, issuer);
    }
}
