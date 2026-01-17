package com.healthfamily.web.controller;

import com.healthfamily.service.CollaborationService;
import com.healthfamily.web.dto.Result;
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

public class CollaborationControllerAuthTest {

    private MockMvc mockMvc;
    private CollaborationService collaborationService;

    @BeforeEach
    void setup() {
        collaborationService = Mockito.mock(CollaborationService.class);
        CollaborationController controller = new CollaborationController(collaborationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new com.healthfamily.web.GlobalExceptionHandler())
                .build();
    }

    @Test
    void abnormalToday_FallbackHeader_WhenPrincipalNull() throws Exception {
        when(collaborationService.getAbnormalToday(anyLong(), anyLong()))
                .thenReturn(new com.healthfamily.web.dto.HomeAbnormalTodayResponse(1L, 0, java.util.List.of()));
        mockMvc.perform(get("/api/families/1/abnormal-today")
                        .header("X-User-Id", "4")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(collaborationService).getAbnormalToday(4L, 1L);
    }

    @Test
    void abnormalToday_Unauthenticated_NoHeader_Should401() throws Exception {
        mockMvc.perform(get("/api/families/1/abnormal-today").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
