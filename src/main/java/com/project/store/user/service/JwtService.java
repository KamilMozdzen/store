package com.project.store.user.service;


import com.project.store.user.dto.AuthResponse;
import com.project.store.user.entity.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

    private static final String ISSUER = "store-api";

    private final JwtEncoder jwtEncoder;
    private final Duration expiration;

    public JwtService(JwtEncoder jwtEncoder,
                      @Value("${app.jwt.expiration}") Duration expiration) {
        this.jwtEncoder = jwtEncoder;
        this.expiration = expiration;
    }
    public AuthResponse generateToken(AppUser user){
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expiration);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim("userId",user.getId())
                .claim("role", user.getRole().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        return new AuthResponse(
                token,
                "Bearer",
                expiration.toSeconds()
        );
    }
}
