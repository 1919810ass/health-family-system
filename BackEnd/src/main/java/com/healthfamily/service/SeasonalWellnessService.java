package com.healthfamily.service;

import com.healthfamily.web.dto.SeasonalWellnessDTO;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface SeasonalWellnessService {
    SeasonalWellnessDTO getWellnessAdvice(Long userId);

    Flux<ServerSentEvent<String>> getWellnessAdviceStream(Long userId);
}
