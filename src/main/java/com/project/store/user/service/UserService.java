package com.project.store.user.service;

import com.project.store.user.dto.UserRegisterRequest;
import com.project.store.user.dto.UserResponse;
import com.project.store.user.entity.AppUser;
import com.project.store.user.entity.UserRole;
import com.project.store.user.exception.EmailAlreadyUsedException;
import com.project.store.user.exception.InvalidCredentialsException;
import com.project.store.user.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Locale;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if(userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        AppUser user = new AppUser(
                email,
                passwordEncoder.encode(request.password()),
                request.firstName().trim(),
                request.lastName().trim(),
                UserRole.CUSTOMER
        );
        return toResponse(userRepository.save(user));
    }
    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getRole(),
        user.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public UserResponse findCurrentUser(String authenticatedEmail) {
        AppUser user = userRepository
                .findByEmailIgnoreCase(authenticatedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        return toResponse(user);
    }

}
