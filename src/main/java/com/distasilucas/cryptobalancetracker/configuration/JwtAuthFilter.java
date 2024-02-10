package com.distasilucas.cryptobalancetracker.configuration;

import com.distasilucas.cryptobalancetracker.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "security", name = "enabled", havingValue = "true")
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        var authHeader = request.getHeader("Authorization");
        var headerType = StringUtils.hasText(authHeader) ? authHeader.split(" ")[0] : "";

        if (!StringUtils.hasText(authHeader) || isNotBearerToken(headerType)) {
            filterChain.doFilter(request, response);
            return;
        }

        var jwtToken = authHeader.split(" ")[1];
        var username = jwtService.extractUsername(jwtToken);

        if (StringUtils.hasText(username) && isNotAlreadyAuthenticated()) {
            var userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwtToken, userDetails)) {
                var authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );

                var webAuthenticationDetails = new WebAuthenticationDetailsSource().buildDetails(request);
                authenticationToken.setDetails(webAuthenticationDetails);

                var securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isNotBearerToken(String authHeader) {
        return "Bearer".equalsIgnoreCase(authHeader);
    }

    private boolean isNotAlreadyAuthenticated() {
        return null == SecurityContextHolder.getContext().getAuthentication();
    }
}
