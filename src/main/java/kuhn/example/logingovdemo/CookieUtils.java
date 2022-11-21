package kuhn.example.logingovdemo;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {
    public static final String JWT_NAME = "kuhn.example.cookie.jwt";
    public static final String ACCESS_NAME = "kuhn.example.cookie.access";
    public static final String STATE_NAME = "kuhn.example.cookie.state";
    public static final String NONCE_NAME = "kuhn.example.cookie.nonce";

    private static final int fifteenMinutes = 60 * 15;

    public static String getHttpCookie(final ServletRequest req, final String key) {
        System.out.println(String.format("Getting cookie: [%s]", key));
        for (final Cookie c : ((HttpServletRequest)req).getCookies()) {
            if (!key.equals(c.getName())) {
                continue;
            }

            System.out.println(String.format("Found cookie: [%s]: [%s]", key, c.getValue()));
            return c.getValue();
        }

        System.out.println(String.format("Cookie does not exist: [%s]", key));
        throw new IllegalArgumentException(String.format("Did not find [%s] in cookies", key));
    }

    public static void setHttpCookie(final ServletResponse res, final String key, final String value) {
        System.out.println(String.format("Setting cookie: [%s], [%s]", key, value));
        final Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(fifteenMinutes);
        
        ((HttpServletResponse) res).addCookie(cookie);
    }
}