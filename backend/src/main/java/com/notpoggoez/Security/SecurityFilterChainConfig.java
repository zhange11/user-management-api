package com.notpoggoez.Security;


import com.notpoggoez.jwt.JWTAuthFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityFilterChainConfig {
    private final AuthenticationProvider authenticationProvider;
    private final JWTAuthFilter jwtAuthFilter;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final OAuth2LoginHandler oAuth2LoginHandler;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;
    public SecurityFilterChainConfig(AuthenticationProvider authenticationProvider, JWTAuthFilter jwtAuthFilter,
                                     @Qualifier("delegatedAuthEntryPoint") AuthenticationEntryPoint authenticationEntryPoint,
                                     OAuth2LoginHandler oAuth2LoginHandler, ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
        this.oAuth2LoginHandler = oAuth2LoginHandler;
        this.clientRegistrationRepositoryProvider = clientRegistrationRepositoryProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/customers",
                        "/api/v1/auth/login"
                )
                .permitAll()
                .requestMatchers(
                        HttpMethod.GET,
                        "/ping",
                        "/api/v1/customers/*/profile-image"
                )
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/**")
                .permitAll()
                                .requestMatchers("/oauth2/**", "/login/oauth2/**")
                                .permitAll()
                                .anyRequest().authenticated()

                )

                .authenticationProvider(authenticationProvider)
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
        if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2
                                      .successHandler(oAuth2LoginHandler)
            );
        }

        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
        );
        return http.build();
    }
}
