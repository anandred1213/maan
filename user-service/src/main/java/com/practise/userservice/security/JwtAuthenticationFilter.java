package com.practise.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * 
 * This filter intercepts every HTTP request and validates JWT tokens.
 * It runs once per request before the controller is invoked.
 * 
 * Flow:
 * 1. Extract Authorization header from request
 * 2. Check if header starts with "Bearer "
 * 3. Extract JWT token (remove "Bearer " prefix)
 * 4. Validate token using JwtUtil
 * 5. If valid, extract user email from token
 * 6. Create authentication object and set in SecurityContext
 * 7. Continue filter chain to next filter/controller
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil JwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil JwtUtil, CustomUserDetailsService userDetailsService) {
        this.JwtUtil = JwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Step 1: Extract Authorization header
        String authHeader = request.getHeader("Authorization");
        
        // Step 2 & 3: Check if header exists and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            if (token.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // Step 4: Validate token
            if (JwtUtil.validateToken(token)) {
                // Step 5: Extract email from token
                String email = JwtUtil.extractEmail(token);
                
                // Step 6: Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                // Step 7: Create authentication object with user details and authorities
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        // Step 8: Continue to next filter or controller
        filterChain.doFilter(request, response);

    }
}
