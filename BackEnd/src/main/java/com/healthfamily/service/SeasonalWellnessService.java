package com.healthfamily.service;

import com.healthfamily.web.dto.SeasonalWellnessDTO;
import org.springframework.http.codec.ServerSentEvent;
/**
 * SeasonalWellness服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import reactor.core.publisher.Flux;

public interface SeasonalWellnessService {
    SeasonalWellnessDTO getWellnessAdvice(Long userId);

    Flux<ServerSentEvent<String>> getWellnessAdviceStream(Long userId);
}
