package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.exception.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

import static com.distasilucas.cryptobalancetracker.constants.Constants.UNKNOWN_ERROR;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.TOKEN_EXPIRED;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "security", name = "enabled", havingValue = "true")
public class JwtService {

    public final String jwtSigningKey;

    public JwtService(@Value("${jwt.signing-key}") String jwtSigningKey) {
        this.jwtSigningKey = jwtSigningKey;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.info("Validating JWT token for {}", userDetails.getUsername());

        return isTokenNonExpired(token) && extractUsername(token).equals(userDetails.getUsername());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenNonExpired(String token) {
        var date = extractClaim(token, Claims::getExpiration);
        return date.after(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claims) {
        Claims claim = extractClaims(token);
        return claims.apply(claim);
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, TOKEN_EXPIRED);
        } catch (Exception ex) {
            log.info("Exception when parsing JWT Token {}", ex.getMessage());
            throw new ApiException(UNKNOWN_ERROR, ex);
        }
    }

    private SecretKey getSigningKey() {
        var decoders = Decoders.BASE64.decode(jwtSigningKey);

        return Keys.hmacShaKeyFor(decoders);
    }
}
