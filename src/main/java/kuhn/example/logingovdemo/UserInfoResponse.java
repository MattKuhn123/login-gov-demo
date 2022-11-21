package kuhn.example.logingovdemo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserInfoResponse {
    @JsonProperty("email_verified")
    private boolean emailVerified;
    @JsonProperty("email")
    private String email;

    public boolean getEmailVerified() {
        return emailVerified;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return String.format("email: [%s], emailVerified: [%s]", email, emailVerified);
    }
}
