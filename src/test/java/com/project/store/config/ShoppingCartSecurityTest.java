package com.project.store.config;

import com.project.store.cart.controller.ShoppingCartController;
import com.project.store.cart.service.ShoppingCartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShoppingCartController.class)
@Import(SecurityConfig.class)
class ShoppingCartSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShoppingCartService cartService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void rejectsAnonymousCartAccess() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(cartService);
    }

    @Test
    void allowsAuthenticatedCartAccess() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .with(jwt().jwt(token -> token
                                .subject("jan@example.com"))))
                .andExpect(status().isOk());

        verify(cartService).getCart("jan@example.com");
    }
}