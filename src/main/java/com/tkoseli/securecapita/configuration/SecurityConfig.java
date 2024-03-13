package com.tkoseli.securecapita.configuration;

import com.tkoseli.securecapita.enumeration.RoleType;
import com.tkoseli.securecapita.filter.CustomAutherizationFilter;
import com.tkoseli.securecapita.handler.CustomAccessDeniedHandler;
import com.tkoseli.securecapita.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;
import static jakarta.servlet.DispatcherType.ERROR;
import static jakarta.servlet.DispatcherType.FORWARD;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final BCryptPasswordEncoder encoder;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final CustomAutherizationFilter customAuthorizationFilter;
    private static final int STRENGTH = 12;

    private static final String[] PUBLIC_URLS = { "/user/login/**", "/user/verify/code/**"  };

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(configure -> configure.configurationSource(null))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception ->
                        exception.accessDeniedHandler(customAccessDeniedHandler)
                                .authenticationEntryPoint(customAuthenticationEntryPoint))
               .authorizeHttpRequests(request  ->
                       request.requestMatchers(PUBLIC_URLS).permitAll()
                       .requestMatchers(HttpMethod.DELETE, "/user/delete/**")
                               .hasAnyAuthority("DELETE:USER")
                       .requestMatchers(HttpMethod.DELETE, "/customer/delete/**")
                               .hasAnyAuthority("DELETE:CUSTOMER")
                               .anyRequest().authenticated())
                .addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(/*UserDetailsService userDetailsService, BCryptPasswordEncoder passwordEncoder*/) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(/*passwordEncoder*/ encoder);


        //providerManager.setEraseCredentialsAfterAuthentication(false);
        return new ProviderManager(authProvider); //providerManager;
    }



/* Documentations
https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/authentication/UsernamePasswordAuthenticationFilter.html
https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authentication/AuthenticationManager.html
https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authentication/AuthenticationProvider.html
https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authentication/dao/DaoAuthenticationProvider.html
https://docs.spring.io/spring-security/site/docs/current/api/org/spningframework/secunity/authentication/dao/AbstractUserDetailsAuthenticationProviden.html
*/


}
