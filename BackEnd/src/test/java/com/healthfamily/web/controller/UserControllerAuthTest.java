package com.healthfamily.web.controller;

import com.healthfamily.service.UserService;
import com.healthfamily.web.dto.Result;
import com.healthfamily.web.dto.UserProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerAuthTest {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setup() {
        userService = Mockito.mock(UserService.class);
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getProfile_FallbackHeader_WhenPrincipalNull() throws Exception {
        when(userService.getProfile(anyLong()))
                .thenReturn(new UserProfileResponse(4L, "13800000000", "Tester", null, null, "MEMBER", java.util.Map.of()));

        mockMvc.perform(get("/api/user/profile")
                        .header("X-User-Id", "4")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).getProfile(4L);
    }

    @Test
    void getProfile_Unauthenticated_NoHeader_Should401() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setControllerAdvice(new com.healthfamily.web.GlobalExceptionHandler())
                .build();
        mockMvc.perform(get("/api/user/profile").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
