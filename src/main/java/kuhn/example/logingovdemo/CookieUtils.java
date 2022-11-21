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
    public static final String AUTHENTICATED_NAME = "kuhn.example.cookie.authenticated";
    public static final int FIFTEEN_MINUTES = 15 * 60;

    public static String getCookie(final ServletRequest req, final String key) {
        System.out.println(String.format("Getting cookie: [%s]", key));
        final HttpServletRequest httpReq = (HttpServletRequest)req;
        final Cookie[] cookies = httpReq.getCookies();
        for (final Cookie c : cookies) {
            if (!key.equals(c.getName())) {
                continue;
            }

            System.out.println(String.format("Found cookie: [%s]: [%s]", key, c.getValue()));
            return c.getValue();
        }

        System.out.println(String.format("Cookie does not exist: [%s]", key));
        throw new IllegalArgumentException(String.format("Did not find [%s] in cookies", key));
    }

    public static void setHttpCookie(final ServletResponse res, final String key, final String value, final int expiresIn) {
        System.out.println(String.format("Setting cookie: [%s], [%s]", key, value));
        final Cookie c = new Cookie(key, value);
        c.setHttpOnly(true);
        c.setMaxAge(expiresIn);
        
        ((HttpServletResponse) res).addCookie(c);
    }

    public static void setClientCookie(final ServletResponse res, final String key, final String value, final int expiresIn) {
        System.out.println(String.format("Setting cookie: [%s], [%s]", key, value));
        final Cookie c = new Cookie(key, value);
        c.setHttpOnly(false);
        c.setMaxAge(expiresIn);
        
        ((HttpServletResponse) res).addCookie(c);
    }

    public static void deleteCookie(final ServletResponse res, final String key) {
        System.out.println(String.format("Deleting cookie: [%s]", key));
        final Cookie c = new Cookie(key, "");
        c.setHttpOnly(true);
        c.setMaxAge(0);
        
        ((HttpServletResponse) res).addCookie(c);
    }
}
