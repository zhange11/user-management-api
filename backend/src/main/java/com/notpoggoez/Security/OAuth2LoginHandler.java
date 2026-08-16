package com.notpoggoez.Security;

import com.notpoggoez.Customer.Customer;
import com.notpoggoez.Customer.CustomerDao;
import com.notpoggoez.Customer.Gender;
import com.notpoggoez.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * On successful Google/GitHub login, ensures a Customer record exists for the
 * OAuth email, issues our own JWT, and redirects back to the frontend with it.
 */
@Component
public class OAuth2LoginHandler implements AuthenticationSuccessHandler {

    private final CustomerDao customerDao;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public OAuth2LoginHandler(@Qualifier("jpa") CustomerDao customerDao,
                                     JWTUtil jwtUtil,
                                     PasswordEncoder passwordEncoder) {
        this.customerDao = customerDao;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // GitHub only returns "email" if the account has a public email and the
        // user:email scope was granted. Fall back to a stable placeholder.
        if (email == null) {
            String login = oAuth2User.getAttribute("login");
            email = login + "@users.noreply.github.com";
        }

        if (!customerDao.existsCustomerByEmail(email)) {
            Customer newCustomer = new Customer(
                    name != null ? name : email,
                    email,
                    18, // TODO: collect real age/gender via a post-signup profile step
                    Gender.MALE,
                    passwordEncoder.encode(UUID.randomUUID().toString())
            );
            customerDao.insertCustomer(newCustomer);
        }

        String token = jwtUtil.issueToken(email, "ROLE_USER");
        response.sendRedirect(frontendUrl + "/oauth/callback?token=" + token);
    }
}