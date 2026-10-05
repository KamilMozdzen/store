package com.project.store.user.service;

import com.project.store.user.dto.AuthResponse;
import com.project.store.user.dto.UserLoginRequest;
import com.project.store.user.entity.AppUser;
import com.project.store.user.entity.UserRole;
import com.project.store.user.exception.InvalidCredentialsException;
import com.project.store.user.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void logsInWithValidCredentials() {
        UserLoginRequest request = new UserLoginRequest(
                "  KAMIL@example.com  ",
                "BezpieczneHaslo123!"
        );

        AppUser user = new AppUser(
                "kamil@example.com",
                "encoded-password",
                "Kamil",
                "Mozdzen",
                UserRole.CUSTOMER
        );

        AuthResponse expectedResponse = new AuthResponse(
                "jwt-token",
                "Bearer",
                3600
        );

        when(userRepository.findByEmailIgnoreCase("kamil@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(
                "BezpieczneHaslo123!",
                "encoded-password"
        )).thenReturn(true);
        when(jwtService.generateToken(user))
                .thenReturn(expectedResponse);

        AuthResponse response = authService.login(request);

        assertEquals(expectedResponse, response);
        verify(jwtService).generateToken(user);
    }

    @Test
    void rejectsUnknownEmail() {
        UserLoginRequest request = new UserLoginRequest(
                "missing@example.com",
                "BezpieczneHaslo123!"
        );

        when(userRepository.findByEmailIgnoreCase("missing@example.com"))
                .thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void rejectsIncorrectPassword() {
        UserLoginRequest request = new UserLoginRequest(
                "kamil@example.com",
                "NiepoprawneHaslo"
        );

        AppUser user = new AppUser(
                "kamil@example.com",
                "encoded-password",
                "Kamil",
                "Mozdzen",
                UserRole.CUSTOMER
        );

        when(userRepository.findByEmailIgnoreCase("kamil@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(
                "NiepoprawneHaslo",
                "encoded-password"
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(jwtService);
    }
}