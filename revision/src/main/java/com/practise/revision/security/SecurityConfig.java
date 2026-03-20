package com.practise.revision.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Security Filter Chain Configuration
     * 
     * Flow:
     * 1. Request arrives at the application
     * 2. CSRF protection is disabled (stateless API)
     * 3. Authorization rules are checked:
     *    - /auth/** endpoints (login, register) are public
     *    - /actuator/** endpoints (health checks) are public
     *    - All other endpoints require authentication
     * 4. Session management is STATELESS (no server-side sessions)
     * 5. JwtAuthenticationFilter runs before UsernamePasswordAuthenticationFilter
     *    - Extracts JWT token from Authorization header
     *    - Validates token and sets authentication in SecurityContext
     * 6. If authenticated, request proceeds to controller
     * 7. If not authenticated, returns 401 Unauthorized
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
            // Step 1: Disable CSRF for stateless REST API
            .csrf(csrf -> csrf.disable())

            
            // Step 2: Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // allow preflight
                .requestMatchers("/auth/**", "/actuator/**","/users/register","/users/login")
                    .permitAll()  // Public endpoints
                .anyRequest().authenticated()  // All other endpoints require authentication
            )
            
            // Step 3: Set session management to STATELESS (JWT-based, no sessions)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Step 4: Add JWT filter before Spring Security's default authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * Password Encoder Bean
     * Uses BCrypt hashing algorithm for secure password storage
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Provider
     * Configures how authentication is performed using UserDetailsService
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Authentication Manager Bean
     * Required for manual authentication (e.g., login endpoint)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
