package com.project.store.user.controller;

import com.project.store.user.dto.UserResponse;
import com.project.store.user.entity.UserRole;
import com.project.store.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void returnsCurrentUser() throws Exception {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "kamil@example.com",
                        null,
                        Collections.emptyList()
                );

        when(userService.findCurrentUser("kamil@example.com"))
                .thenReturn(new UserResponse(
                        1L,
                        "kamil@example.com",
                        "Kamil",
                        "Mozdzen",
                        UserRole.CUSTOMER,
                        Instant.parse("2026-10-10T10:00:00Z")
                ));

        mockMvc.perform(get("/api/users/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email")
                        .value("kamil@example.com"))
                .andExpect(jsonPath("$.firstName")
                        .value("Kamil"))
                .andExpect(jsonPath("$.lastName")
                        .value("Mozdzen"))
                .andExpect(jsonPath("$.role")
                        .value("CUSTOMER"))
                .andExpect(jsonPath("$.password")
                        .doesNotExist())
                .andExpect(jsonPath("$.passwordHash")
                        .doesNotExist());

        verify(userService).findCurrentUser(
                "kamil@example.com"
        );
    }
}