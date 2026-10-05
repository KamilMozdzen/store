package com.project.store.user.service;

import com.project.store.user.dto.AuthResponse;
import com.project.store.user.dto.UserLoginRequest;
import com.project.store.user.entity.AppUser;
import com.project.store.user.exception.InvalidCredentialsException;
import com.project.store.user.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(UserLoginRequest request){
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new InvalidCredentialsException();
        }

        return jwtService.generateToken(user);
    }
}
