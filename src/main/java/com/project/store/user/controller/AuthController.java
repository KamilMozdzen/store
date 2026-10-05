package com.project.store.user.controller;

import com.project.store.user.dto.AuthResponse;
import com.project.store.user.dto.UserLoginRequest;
import com.project.store.user.dto.UserRegisterRequest;
import com.project.store.user.dto.UserResponse;
import com.project.store.user.service.AuthService;
import com.project.store.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @Valid @RequestBody UserRegisterRequest request
            ){return userService.register(request);}

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody UserLoginRequest request
    ){
        return authService.login(request);
    }
}
