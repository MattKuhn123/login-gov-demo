package kuhn.example.logingovdemo;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Utils {
    public static final String JWT_NAME = "kuhn.example.jwt";
    public static final String STATE_NAME = "kuhn.example.state";
    public static final String NONCE_NAME = "kuhn.example.nonce";

    public static String getCookie(final ServletRequest request, final String key) {
        System.out.println(String.format("Searching for cookie: [%s]", key));
        for (final Cookie c : ((HttpServletRequest)request).getCookies()) {
            if (!key.equals(c.getName())) {
                continue;
            }

            System.out.println(String.format("Found cookie: [%s]: [%s]", key, c.getValue()));
            return c.getValue();
        }

        System.out.println(String.format("Did not find cookie: [%s]", key));
        throw new IllegalArgumentException(String.format("Did not find [%s] in cookies", key));
    }

    public static void setHttpCookie(final ServletResponse response, final String key, final String value) {
        System.out.println(String.format("Setting cookie: [%s], [%s]", key, value));
        final Cookie cookie = new Cookie(key, "");
        cookie.setHttpOnly(true);

        if ("".equals(value)) {
            cookie.setMaxAge(0);
        }

        ((HttpServletResponse) response).addCookie(cookie);
    }
}
