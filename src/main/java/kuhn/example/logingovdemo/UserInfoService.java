package kuhn.example.logingovdemo;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserInfoService {
    
    private final RestTemplate restTemplate;
    private final String loginGovUrl;
    public UserInfoService(final Environment env, final RestTemplateBuilder builder) {
        loginGovUrl = env.getProperty("loginGovUrl");
        restTemplate = builder.build();
    }

    public UserInfoResponse getUserInfo(final ServletRequest req) throws ServletException {
        final String url = String.format("%sapi/openid_connect/userinfo", loginGovUrl);

        final HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", CookieUtils.getCookie(req, CookieUtils.ACCESS_NAME)));
        final HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        System.out.println(String.format("Request: [%s]", url));
        final UserInfoResponse userInfoResponse = restTemplate.exchange(url, HttpMethod.GET, entity, UserInfoResponse.class).getBody();
        if (userInfoResponse == null) {
            throw new ServletException("Failed to get user info");
        }

        System.out.println(String.format("Result: [%s]", userInfoResponse.toString()));
        return userInfoResponse;
    }
}
