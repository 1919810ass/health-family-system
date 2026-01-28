package com.healthfamily.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class OllamaLegacyClient {

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${spring.ai.ollama.chat.options.model:qwen2.5:7b}")
    private String defaultModel;

    @Value("${spring.ai.ollama.chat.options.temperature:0.7}")
    private double defaultTemperature;

    public String generate(String prompt, String model, Double temperature) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model != null && !model.isBlank() ? model : defaultModel);
        body.put("prompt", prompt);
        body.put("stream", false);
        if (temperature != null) {
            body.put("temperature", temperature);
        } else if (defaultTemperature > 0) {
            body.put("temperature", defaultTemperature);
        }
        return webClient()
                .post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(OllamaGenerateResponse.class)
                .map(OllamaGenerateResponse::response)
                .block();
    }

    public Flux<String> generateStream(String prompt, String model, Double temperature) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model != null && !model.isBlank() ? model : defaultModel);
        body.put("prompt", prompt);
        body.put("stream", true);
        if (temperature != null) {
            body.put("temperature", temperature);
        } else if (defaultTemperature > 0) {
            body.put("temperature", defaultTemperature);
        }
        return webClient()
                .post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_NDJSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(OllamaGenerateResponse.class)
                .map(OllamaGenerateResponse::response)
                .filter(Objects::nonNull);
    }

    private WebClient webClient() {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    record OllamaGenerateResponse(String response) {
    }
}
