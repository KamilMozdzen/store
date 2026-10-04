package com.project.store.user.controller;

import com.project.store.user.dto.UserRegisterRequest;
import com.project.store.user.dto.UserResponse;
import com.project.store.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @Valid @RequestBody UserRegisterRequest request
            ){return userService.register(request);}
}
