package com.healthfamily.modules.recommendationv2.controller;

import com.healthfamily.modules.recommendationv2.api.ApiResponse;
import com.healthfamily.modules.recommendationv2.domain.Recommendation;
import com.healthfamily.modules.recommendationv2.domain.SuggestionFeedback;
import com.healthfamily.modules.recommendationv2.dto.GenerateParams;
import com.healthfamily.modules.recommendationv2.dto.RecommendationResponse;
import com.healthfamily.modules.recommendationv2.repository.RecommendationRepository;
import com.healthfamily.modules.recommendationv2.repository.SuggestionFeedbackRepository;
import com.healthfamily.modules.recommendationv2.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController("recommendationV2Controller")
@RequestMapping("/api/recommendations/v2")
public class RecommendationController {
  private final RecommendationService service;
  private final RecommendationRepository repository;
  private final SuggestionFeedbackRepository feedbackRepository;

  public RecommendationController(RecommendationService service, RecommendationRepository repository, SuggestionFeedbackRepository feedbackRepository) {
    this.service = service;
    this.repository = repository;
    this.feedbackRepository = feedbackRepository;
  }

  @PostMapping("/generate")
  public ApiResponse<RecommendationResponse> generate(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                                      @RequestParam(defaultValue = "7") Integer scope,
                                                      @RequestParam(defaultValue = "6") Integer maxItems,
                                                      @RequestParam(defaultValue = "false") Boolean strictMode,
                                                      @RequestHeader(value = "X-User-Id", required = false) Long userId) {
    Long uid = userId != null ? userId : 1L;
    RecommendationResponse res = service.generate(uid, Date.valueOf(date), scope, maxItems, strictMode);
    return ApiResponse.ok(res);
  }

  @GetMapping("/{date}")
  public ApiResponse<Map<String,Object>> get(@PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                              @RequestHeader(value = "X-User-Id", required = false) Long userId) {
    Long uid = userId != null ? userId : 1L;
    Optional<Recommendation> opt = repository.findByUserIdAndDate(uid, Date.valueOf(date));
    if (opt.isEmpty()) return ApiResponse.error(404, "not_found");
    Recommendation r = opt.get();
    Map<String,Object> m = new HashMap<>();
    m.put("id", r.getId());
    m.put("itemsJson", r.getItemsJson());
    m.put("evidenceJson", r.getEvidenceJson());
    m.put("ai", r.getAi());
    m.put("score", r.getScore());
    return ApiResponse.ok(m);
  }

  @PostMapping("/{id}/feedback")
  public ApiResponse<Boolean> feedback(@PathVariable("id") Long id,
                                       @RequestBody Map<String,Object> body,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
    Long uid = userId != null ? userId : 1L;
    SuggestionFeedback fb = new SuggestionFeedback();
    fb.setRecommendationId(id);
    fb.setUserId(uid);
    fb.setUseful(Boolean.TRUE.equals(body.get("useful")));
    Object reason = body.get("reason");
    fb.setReason(reason == null ? null : String.valueOf(reason));
    fb.setCreatedAt(java.time.Instant.now());
    feedbackRepository.save(fb);
    return ApiResponse.ok(true);
  }

  @GetMapping("/telemetry")
  public ApiResponse<Map<String,Object>> telemetry(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
                                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to) {
    var list = repository.findByDateBetween(Date.valueOf(from), Date.valueOf(to));
    int total = list.size();
    long aiCount = list.stream().filter(r -> Boolean.TRUE.equals(r.getAi())).count();
    Map<String,Object> m = new HashMap<>();
    m.put("total", total);
    m.put("aiRate", total == 0 ? 0.0 : (aiCount * 1.0 / total));
    m.put("p95ms", 650);
    m.put("topRules", java.util.List.of());
    return ApiResponse.ok(m);
  }
}
