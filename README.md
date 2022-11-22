# Login.gov demo

## Login.gov application

*This demo application already has an application in login.gov created.*

## How the front-end is authenticated

The homepage is `src/main/resources/templates/index.html`.  
It will render itself *(server-side)* depending on an `{ "authenticated": boolean }` attribute.  
This attribute is calculated based on the presence and expiration status of a `JWT token` from login.gov that is stored as an http-only cookie.

``` java
    @GetMapping("/")
    public ModelAndView index(final ServletRequest req) {
        final ModelAndView m = new ModelAndView();
        m.setViewName("index");
        try {
            final DecodedJWT decodedJWT = JWT.decode(CookieUtils.getCookie(req, CookieUtils.JWT_NAME));
            m.addObject("authenticated", new Date(Instant.now().toEpochMilli()).before((decodedJWT.getExpiresAt())));
        } catch (final Exception e) {
            m.addObject("authenticated", false);
        }

        return m;
    }
```
  
``` html
    <div id="display-if-no-auth" class="card bg-light mb-3" style="width: 18rem;" th:if="${!authenticated}">
    ...
    </div>
    ...

    <div id="display-if-auth" class="card text-white bg-dark mb-3" style="width: 18rem;" th:if="${authenticated}">
    ...
    </div>

```

## How the back-end is authenticated

There are two controllers: `kuhn.example.logingovdemo.ControllerUnauth` and `kuhn.example.logingovdemo.ControllerAuth`.  
The `authenticated` controller is guarded by `kuhn.example.logingovdemo.FilterAuth`.  
`kuhn.example.logingovdemo.ControllerAuth`'s endpoints are mapped to `/auth`, and `kuhn.example.logingovdemo.FilterAuth` automatically intercepts all requests to `/auth/*`  

``` java
@RestController
@RequestMapping("/auth")
public class ControllerAuth {
    ...
}
```

``` java
@Component
public class FilterAuth implements Filter {
    ...

    @Bean
    public FilterRegistrationBean<FilterAuth> authFilter() {
        FilterRegistrationBean<FilterAuth> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("/auth/*");
        return registrationBean; 
    }
}
```

## How to authenticate

1. **User clicks button on the application's front-end**
    - If the `authorized` attribute is false, `src/main/resources/templates/index.html` displays a login button. Clicking it sends a request to the application's `/login` endpoint.

    ```html
    <div id="display-if-no-auth" class="card bg-light mb-3" style="width: 18rem;" th:if="${!authenticated}">
        <div class="card-body">
            <h5 class="card-title">Unauthenticated</h5>
            <p class="card-text">This is displayed only when you aren't logged in</p>
            <a class="btn btn-primary" hx-trigger="click" hx-post="/login">Login</a>
        </div>
    </div>
    ```

1. **The application receives button-click's request**
    - The `/login` endpoint is in `kuhn.example.logingovdemo.ControllerUnauth`  

    ``` java
    @PostMapping("/login")
    public void login(final ServletRequest req, final ServletResponse res) {
        System.out.println("enter [/login]");
        ...
    }
    ```

1. **The application redirects to login.gov**
    - The application creates a `nonce` and a `state` and stores them in the session's http-only cookies. *(Both will be used to validate the redirects, responses, and tokens that login.gov sends back).*
    - The application gives login.gov a `loginRedirectUri` to send the user to when finished logging in.  

    ``` java
    @PostMapping("/login")
    public void login(final ServletRequest req, final ServletResponse res) {
        System.out.println("enter [/login]");

        final UUID nonce = java.util.UUID.randomUUID();
        CookieUtils.setHttpCookie(res, CookieUtils.NONCE_NAME, nonce.toString(), CookieUtils.FIFTEEN_MINUTES);
        
        final UUID state = java.util.UUID.randomUUID();
        CookieUtils.setHttpCookie(res, CookieUtils.STATE_NAME, state.toString(), CookieUtils.FIFTEEN_MINUTES);

        final String forwardToLogingov = String.format("%s/openid_connect/authorize?"
                + "acr_values=http://idmanagement.gov/ns/assurance/ial/1&"
                + "client_id=%s&"
                + "nonce=$%s&"
                + "prompt=select_account&"
                + "redirect_uri=%s&"
                + "response_type=code&"
                + "scope=openid+email&"
                + "state=%s", loginGovUrl, clientId, nonce, loginRedirectUri, state);
        System.out.println("forwarding to: " + forwardToLogingov);
        // forward user to logingov

        ...
        (continues below)
    ```

    - The application forwards the user to login.gov to enter their credentials and MFA.

    ``` java
        ...
        (continued from above)

        // forward user to logingov
        ((HttpServletResponse) res).setHeader("HX-Redirect", forwardToLogingov);

        System.out.println("exit [/login]");
    }
    ```

1. **Login.gov redirects back to the application**
    - Login.gov `redirects` the user back to the `loginRedirectUri` specified in a previous step.
    - Login.gov's `redirect` passed the `state` back to the application. The application verifies the request by making sure that it is the same `state` as what is stored in the session's http-only cookies.  
    - Login.gov's `redirect` passed a `code` that the application will use to request a `JWT token`.

    ``` java
    @GetMapping("/redirectLogin")
    public RedirectView redirectLogin(final ServletRequest req, final ServletResponse res, @RequestParam String code, @RequestParam String state, @RequestParam(defaultValue = "", required = false) String error) 
            throws ServletException, IOException {
        System.out.println(String.format("enter [/redirectLogin], code: [%s], state: [%s], error: [%s]", code, state, error));
        if (!StringUtils.isEmptyOrWhitespace(error)) {
            return new RedirectView("error");
        }

        if (!state.equals(CookieUtils.getCookie(req, CookieUtils.STATE_NAME))) {
            System.out.println("State invalid");
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "State invalid");
            return new RedirectView("");
        } else {
            System.out.println("State valid");
        }

        // use code to request JWT token

        ...
        (continues below)
    }
    ```

    - The application uses the `code` from login.gov to request a `JWT token` and an `access token`.

    ``` java
        ...
        (continued from above)

        // use code to request JWT token
        final TokenResponse jwtResponse = tokenService.getToken(code);

        // validate JWT token
        ...
        (continues below)
    }
    ```

    - The application validates that the token returned from login.gov has the same `nonce` as what it stored in the session's http-only cookies.
    - The application stores the `JWT token` and `access token` in the session's http-only cookies.

    ``` java
        ...
        (continued from above)

        // validate JWT token
        if (!jwtResponse.getNonce().equals(CookieUtils.getCookie(req, CookieUtils.NONCE_NAME))) {
            System.out.println("Nonce invalid");
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Nonce invalid");
            return new RedirectView("");
        } else {
            System.out.println("Nonce valid");
        }

        CookieUtils.setHttpCookie(res, CookieUtils.JWT_NAME, jwtResponse.getEncodedIdToken(), jwtResponse.getExpiresIn());
        CookieUtils.setHttpCookie(res, CookieUtils.ACCESS_NAME, jwtResponse.getAccessToken(), jwtResponse.getExpiresIn());

        System.out.println("exit [/redirectLogin]");
        return new RedirectView("");
    }
    ```

1. **The user is now authenticated**
    - The mapping to `src/main/resources/templates/index.html` always evaluates the state of the client's authentication. `authenticated` now resolves to `true`.

    ``` java
    @GetMapping("/")
    public ModelAndView index(final ServletRequest req) {
        ModelAndView m = new ModelAndView();
        m.setViewName("index");
        try {
            final DecodedJWT decodedJWT = JWT.decode(CookieUtils.getCookie(req, CookieUtils.JWT_NAME));
            m.addObject("authenticated", new Date(Instant.now().toEpochMilli()).before((decodedJWT.getExpiresAt())));
        } catch (final Exception e) {
            m.addObject("authenticated", false);
        }

        return m;
    }
    ```

    - This block will now be rendered:
        - *This logic is handled server-side, so it would never be exposed to the client if they weren't authenticated*

    ```html
    <div id="display-if-auth" class="card text-white bg-dark mb-3" style="width: 18rem;" th:if="${authenticated}">
        <div class="card-body">
            <h5 class="card-title">Authenticated</h5>
            <p class="card-text">This is displayed after logging in</p>
            <p class="card-text" hx-trigger="load, every 1s" hx-get="/auth/expires">expiration countdown</p>
            <div style="display: flex;flex-wrap: wrap;flex-direction: column;gap: 10px;">
                <a class="btn btn-danger" hx-trigger="click" hx-get="/auth/random">(auth) Random number</a>
                <a class="btn btn-danger" hx-trigger="click" hx-get="/auth/email">Get user email</a>
                <a class="btn btn-primary" hx-trigger="click" hx-post="/login">Refresh Login</a>
                <a class="btn btn-danger" hx-trigger="click" hx-post="/logout">Logout</a>
            </div>
        </div>
    </div>
    ```

    - All requests to endpoints beginning with `/auth` will successfully pass through the authenticated filter.

    ``` java
    @Component
    public class FilterAuth implements Filter {
        ...

        @Override
        public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
            System.out.println(String.format("enter [%s]", getClass().getName()));

            final DecodedJWT decodedJWT = JWT.decode(CookieUtils.getCookie(req, CookieUtils.JWT_NAME));
            if (!decodedJWT.getIssuer().equals(loginGovUrl)) {
                System.out.println("Invalid issuer");
                ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid issuer");
                return;
            }

            if (!decodedJWT.getClaim("aud").asString().equals(clientId)) {
                System.out.println("Invalid audience");
                ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid audience");
                return;
            }

            if (new Date(Instant.now().toEpochMilli()).after((decodedJWT.getExpiresAt()))) {
                System.out.println("Token expired");
                ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                return;
            }

            if (!decodedJWT.getClaim("nonce").asString().substring(1).equals(CookieUtils.getCookie(req, CookieUtils.NONCE_NAME))) {
                System.out.println("Invalid nonce");
                ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid nonce");
                return;
            }

            chain.doFilter(req, res);

            System.out.println(String.format("exit [%s]", getClass().getName()));
        }
        ...
    }
    ```

1. **Authentication expires after 15 minutes unless refreshed by logging-in again**
    - The user can click `Refresh Login` to extend their `JWT token`'s life.

    ``` html
    ...

    <a class="btn btn-primary" hx-trigger="click" hx-post="/login">Refresh Login</a>
    ...
    ```

    - Notice that it calls the `/login` endpoint. The logic to *refresh* the `JWT token` is the exact same as to *create* it.
    - Login.gov will not require the user to re-enter their credentials if the `JWT token` is not expired.

## Warning

Every security scheme requires diligence from the developer.  
If something on the front-end should be secured, then the developer will have to prevent it from reaching the DOM with `th:if="${authenticated}"`.  
If something on the back-end should be secured, then the developer will have to make sure that the mapping begins with `/auth`.  
No security scheme is fool-proof; the developer will need to be vigilant.
