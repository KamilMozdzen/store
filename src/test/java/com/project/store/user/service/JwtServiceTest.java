package com.project.store.user.service;

import com.project.store.user.dto.AuthResponse;
import com.project.store.user.entity.AppUser;
import com.project.store.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void generatesSignedJwtWithUserClaims() {
        byte[] keyBytes = new byte[32];

        for (int index = 0; index < keyBytes.length; index++) {
            keyBytes[index] = (byte) (index + 1);
        }

        SecretKey secretKey =
                new SecretKeySpec(keyBytes, "HmacSHA256");

        NimbusJwtEncoder encoder =
                NimbusJwtEncoder.withSecretKey(secretKey)
                        .algorithm(MacAlgorithm.HS256)
                        .build();

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withSecretKey(secretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        JwtService jwtService = new JwtService(
                encoder,
                Duration.ofHours(1)
        );

        AppUser user = new AppUser(
                "kamil@example.com",
                "encoded-password",
                "Kamil",
                "Mozdzen",
                UserRole.CUSTOMER
        );

        ReflectionTestUtils.setField(user, "id", 42L);

        AuthResponse response = jwtService.generateToken(user);
        Jwt jwt = decoder.decode(response.accessToken());

        Number userId = jwt.getClaim("userId");

        assertAll(
                () -> assertEquals("Bearer", response.tokenType()),
                () -> assertEquals(3600, response.expiresIn()),
                () -> assertFalse(response.accessToken().isBlank()),
                () -> assertEquals(
                        "kamil@example.com",
                        jwt.getSubject()
                ),
                () -> assertEquals(
                        "store-api",
                        jwt.getClaimAsString("iss")
                ),
                () -> assertEquals(42L, userId.longValue()),
                () -> assertEquals(
                        "CUSTOMER",
                        jwt.getClaimAsString("role")
                ),
                () -> assertNotNull(jwt.getIssuedAt()),
                () -> assertNotNull(jwt.getExpiresAt()),
                () -> assertEquals(
                        3600,
                        Duration.between(
                                jwt.getIssuedAt(),
                                jwt.getExpiresAt()
                        ).toSeconds()
                )
        );
    }
}