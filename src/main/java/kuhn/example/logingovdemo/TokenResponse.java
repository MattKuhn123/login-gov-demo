package kuhn.example.logingovdemo;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("expires_in")
    private int expiresIn;
    @JsonProperty("id_token")
    private String encodedIdToken;

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
        final DecodedJWT decodedJWT = JWT.decode(encodedIdToken);
        return decodedJWT.getIssuer();
    }

    public String getNonce() {
        final DecodedJWT decodedJWT = JWT.decode(encodedIdToken);
        return decodedJWT.getClaim("nonce").asString().substring(1);
    }

    @Override
    public String toString() {
        // return String.format("access_token: [%s] token_type: [%s], expires_in: [%s], encodedIdToken: [%s]", accessToken, tokenType, expiresIn, encodedIdToken);
        return String.format("access_token: [%s]", accessToken);
    }
}
