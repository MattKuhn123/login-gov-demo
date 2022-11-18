package kuhn.example.logingovdemo;

import java.util.Base64;

import org.codehaus.jettison.json.JSONObject;

public class TokenResponse {

    private final String accessToken;
    private final String tokenType;
    private final int expiresIn;
    private final String nonce;

    public TokenResponse(final JSONObject jsonObject) throws Exception {
        accessToken = (String) jsonObject.get("access_token");
        tokenType = (String) jsonObject.get("token_type");
        expiresIn = (int) jsonObject.get("expires_in");
        final JSONObject idToken = new JSONObject(new String(Base64.getDecoder().decode((String) jsonObject.get("id_token"))));
        nonce = (String) idToken.get("nonce");
        System.out.println(idToken);
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

    public String getNonce() {
        return nonce;
    }

    @Override
    public String toString() {
        return String.format("access_token: [%s] token_type: [%s], expires_in: [%s], nonce: [%s]", accessToken, tokenType, expiresIn, nonce);
    }
}
