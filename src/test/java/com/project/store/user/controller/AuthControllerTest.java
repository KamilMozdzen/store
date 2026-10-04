package com.project.store.user.controller;

import com.project.store.user.dto.UserRegisterRequest;
import com.project.store.user.dto.UserResponse;
import com.project.store.user.entity.UserRole;
import com.project.store.user.exception.EmailAlreadyUsedException;
import com.project.store.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void registersUser() throws Exception {
        when(userService.register(any(UserRegisterRequest.class)))
                .thenReturn(new UserResponse(
                        1L,
                        "kamil@example.com",
                        "Kamil",
                        "Mozdzen",
                        UserRole.CUSTOMER,
                        Instant.parse("2026-10-04T12:00:00Z")
                ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "kamil@example.com",
                                  "password": "BezpieczneHaslo123!",
                                  "firstName": "Kamil",
                                  "lastName": "Mozdzen"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("kamil@example.com"))
                .andExpect(jsonPath("$.firstName").value("Kamil"))
                .andExpect(jsonPath("$.lastName").value("Mozdzen"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.createdAt").value("2026-10-04T12:00:00Z"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void rejectsInvalidRegistrationData() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": "short",
                                  "firstName": "",
                                  "lastName": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.detail")
                        .value("One or more fields are invalid"))
                .andExpect(jsonPath("$.errors.email")
                        .value("Email must be valid"))
                .andExpect(jsonPath("$.errors.password")
                        .value("Password must contain between 8 and 72 characters"))
                .andExpect(jsonPath("$.errors.firstName")
                        .value("First name is required"))
                .andExpect(jsonPath("$.errors.lastName")
                        .value("Last name is required"));

        verifyNoInteractions(userService);
    }

    @Test
    void returnsConflictWhenEmailIsAlreadyRegistered() throws Exception {
        when(userService.register(any(UserRegisterRequest.class)))
                .thenThrow(new EmailAlreadyUsedException("kamil@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "kamil@example.com",
                                  "password": "BezpieczneHaslo123!",
                                  "firstName": "Kamil",
                                  "lastName": "Mozdzen"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email Already Used"))
                .andExpect(jsonPath("$.detail")
                        .value("Email 'kamil@example.com' is already registered"));
    }
}