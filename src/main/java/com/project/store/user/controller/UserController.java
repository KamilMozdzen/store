package com.project.store.user.controller;


import com.project.store.user.dto.UserResponse;
import com.project.store.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse currentUser(
            Authentication authentication
    ){
        return userService.findCurrentUser(authentication.getName());
    }
}
