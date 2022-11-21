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
    // TODO : Replace with your pem
    public static final String PEM_LOCATION = "classpath:security\\private-kuhn-demo.pem"; 


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
