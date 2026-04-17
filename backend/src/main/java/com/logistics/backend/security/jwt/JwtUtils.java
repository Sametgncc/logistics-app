package com.logistics.backend.security.jwt;

import com.logistics.backend.security.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtUtils {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(JwtUtils.class);

    @Value("${backendapi.app.jwtExpirationMs:86400000}") // Default 24h if not set
    private long jwtExpirationMs;

    @Value("${backendapi.app.jwtSecret:MySecretKeyWhichIsVerySecureAndLongEnoughToUseWithHS512Algorithm1234567890}") // Default secret
    private String jwtSecret;

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userDetails =
                (UserDetailsImpl) authentication.getPrincipal();

        return buildToken(userDetails.getUsername(), userDetails.getId());
    }

    public String generateTokenFromUsernameAndId(String username, Long userId){
        return buildToken(username, userId);
    }

    private String buildToken(String username, Long userId) {

        var claims = new HashMap<String, Object>();
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public boolean validateJwtToken(String jwtToken){
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwtToken);
            return true;
        } catch (ExpiredJwtException e) {
            LOGGER.error("Jwt token is expired : {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.error("Jwt token is unsupported : {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOGGER.error("Jwt token is invalid : {}", e.getMessage());
        } catch (SignatureException e) {
            LOGGER.error("Jwt token signature is invalid : {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Jwt is empty : {}", e.getMessage());
        }
        return  false;
    }

    public String getUsernameFromJwtToken(String token){
        return getClaimsFromToken(token).getSubject();
    }

    public Long getUserIdFromJwtToken(String token) {
        Object userId = getClaimsFromToken(token).get("userId");
        if (userId == null) return null;
        return Long.valueOf(userId.toString());
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }

}
