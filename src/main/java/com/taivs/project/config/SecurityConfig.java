package com.taivs.project.config;

import com.taivs.project.security.exception.CustomAccessDeniedHandler;
import com.taivs.project.security.exception.CustomAuthenticationEntryPoint;
import com.taivs.project.security.jwt.JWTFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JWTFilter jwtFilter(){
        return new JWTFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh-token").permitAll()
                        .requestMatchers("/api/package/get-by-id/**").hasAnyRole("SHOP", "ADMIN")
                        .requestMatchers("/api/user/get-user-details/{id}").hasAnyRole("SHOP", "ADMIN")
                        .requestMatchers("/api/user/change-name").hasAnyRole("SHOP", "ADMIN")
                        .requestMatchers("/api/package/get-packages").hasRole("ADMIN")
                        .requestMatchers("/api/package/{id}/status").hasRole("ADMIN")
                        .requestMatchers("/api/package/get-packages-of-user/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/user/active-user/**", "/api/user/deactive-user/**", "/api/user/get-users").hasRole("ADMIN")
                        .requestMatchers("/api/package/**").hasRole("SHOP")
                        .requestMatchers("/api/dashboard/**").hasRole("SHOP")
                        .requestMatchers("/api/product/**").hasRole("SHOP")
                        .requestMatchers("/api/report/**").hasRole("SHOP")
                        .requestMatchers("/api/customer/**").hasRole("SHOP")
                        .requestMatchers("/api/user/**").hasRole("SHOP")
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        http.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8181"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
