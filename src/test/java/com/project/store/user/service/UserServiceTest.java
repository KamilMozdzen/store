package com.project.store.user.service;

import com.project.store.user.dto.UserRegisterRequest;
import com.project.store.user.dto.UserResponse;
import com.project.store.user.entity.AppUser;
import com.project.store.user.entity.UserRole;
import com.project.store.user.exception.EmailAlreadyUsedException;
import com.project.store.user.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registersUserWithEncodedPassword() {
        UserRegisterRequest request = new UserRegisterRequest(
                "  Kamil@Example.com  ",
                "BezpieczneHaslo123!",
                " Kamil ",
                " Mozdzen "
        );

        when(userRepository.existsByEmailIgnoreCase("kamil@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("BezpieczneHaslo123!"))
                .thenReturn("encoded-password");
        when(userRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.register(request);

        ArgumentCaptor<AppUser> userCaptor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(userRepository).save(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();

        assertAll(
                () -> assertEquals("kamil@example.com", response.email()),
                () -> assertEquals("Kamil", response.firstName()),
                () -> assertEquals("Mozdzen", response.lastName()),
                () -> assertEquals(UserRole.CUSTOMER, response.role()),
                () -> assertNotNull(response.createdAt()),
                () -> assertEquals("encoded-password", savedUser.getPasswordHash()),
                () -> assertNotEquals(
                        request.password(),
                        savedUser.getPasswordHash()
                )
        );

        verify(passwordEncoder).encode("BezpieczneHaslo123!");
    }

    @Test
    void rejectsAlreadyRegisteredEmail() {
        UserRegisterRequest request = new UserRegisterRequest(
                "KAMIL@example.com",
                "BezpieczneHaslo123!",
                "Kamil",
                "Mozdzen"
        );

        when(userRepository.existsByEmailIgnoreCase("kamil@example.com"))
                .thenReturn(true);

        EmailAlreadyUsedException exception = assertThrows(
                EmailAlreadyUsedException.class,
                () -> userService.register(request)
        );

        assertEquals(
                "Email 'kamil@example.com' is already registered",
                exception.getMessage()
        );

        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(AppUser.class));
    }
}