package com.finaccess.api.config;

import com.finaccess.api.model.User;
import com.finaccess.api.service.CustomUserDetailsService;
import com.finaccess.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtFilter(JwtService jwtService, CustomUserDetailsService customUserDetailsService){
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        //extract email from token
        String email = jwtService.extractEmail(token);

        //check if email exists and not already authenticated
        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
            User user = (User) customUserDetailsService.loadUserByUsername(email); // (User) to tell compiler that the User iam using is the entity that implementing UserDetails otherwise compiler may think its Springs default User, or we can just write UserDetails user = ....

            if(jwtService.isTokenValid(token, user)){

                //create authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                user.getAuthorities()
                        );

                //tell spring security, user is authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);

    }
}
