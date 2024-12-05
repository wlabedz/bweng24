package com.backend.project.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private JWTAuthEntryPoint authEntryPoint;

    private CustomUserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JWTAuthEntryPoint authEntryPoint){
        this.authEntryPoint = authEntryPoint;
        this.userDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/offices").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/offices").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/reviews").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/found_items/submit").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/found_items").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/found_items/description").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "api/offices/*").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "api/users").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "api/users").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "api/users").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/faqs").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/faqs/ask").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/faqs/{id}/approve").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/faqs/{id}/delete").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/faqs/{id}/update").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/faqs/{id}/update").permitAll()


                        .anyRequest().permitAll());
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter(){
        return new JWTAuthenticationFilter();
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
