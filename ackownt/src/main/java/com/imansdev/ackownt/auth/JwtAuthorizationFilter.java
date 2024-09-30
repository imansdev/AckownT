package com.imansdev.ackownt.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        // Extract JWT and email from the Authorization header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Remove "Bearer " prefix
            email = jwtUtil.getEmailFromToken(jwt);
        }

        // If the email is valid and there is no authentication in the current context
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

            // Validate the token
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                // Create an authentication token with the user's details and authorities
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null,
                                userDetails.getAuthorities());

                // Set additional details on the authentication token (like IP address, session ID,
                // etc.)
                authenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Store the authentication token in the security context
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // Proceed with the filter chain
        chain.doFilter(request, response);
    }
}
