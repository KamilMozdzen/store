package com.project.store.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    private final SecretKey secretKey;

    public JwtConfig(@Value("${app.jwt.secret}") String encodedSecret){
        byte[] keyBytes;

        try{
            keyBytes = Base64.getDecoder().decode(encodedSecret);
        }catch(IllegalArgumentException exception){
            throw new IllegalStateException("JWT_SECRET must be a valid base64 value", exception);
        }
        if(keyBytes.length < 32){
            throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }
    @Bean
    JwtEncoder jwtEncoder(){
        return NimbusJwtEncoder.withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }
    @Bean
    JwtDecoder jwtDecoder(){
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
