package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.LifestyleService;
import com.healthfamily.web.dto.*;
import com.healthfamily.web.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequiredArgsConstructor
/**
 * Lifestyle控制器
 * <p>
 * 提供相关 REST API，负责请求参数校验、鉴权信息提取，并调用服务层完成业务处理。
 * </p>
 */
@RequestMapping("/api/lifestyle")
public class LifestyleController {

    private final LifestyleService lifestyleService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping(value = "/diet/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ImageUploadResponse> uploadDietImage(@AuthenticationPrincipal UserPrincipal principal,
                                          @RequestPart("file") MultipartFile file) {
        Long userId = principal.getUserId();
        return Result.success(lifestyleService.uploadDietImage(userId, file));
    }

    @GetMapping("/files/{userId}/{filename}")
    public ResponseEntity<org.springframework.core.io.Resource> getFile(@PathVariable("userId") Long userId,
                                                                        @PathVariable("filename") String filename) {
        Path path = Paths.get(uploadDir, String.valueOf(userId), filename).toAbsolutePath().normalize();
        java.io.File file = path.toFile();

        if (file.exists() && file.canRead()) {
            FileSystemResource resource = new FileSystemResource(file);
            MediaType contentType = MediaType.IMAGE_JPEG;
            String lower = filename.toLowerCase();
            if (lower.endsWith(".png")) contentType = MediaType.IMAGE_PNG;
            else if (lower.endsWith(".webp")) contentType = MediaType.parseMediaType("image/webp");
            else if (lower.endsWith(".gif")) contentType = MediaType.IMAGE_GIF;

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .body(resource);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/diet/ingest")
    /**
     * 执行业务操作
     * @param principal 当前登录用户
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<DietIngestResponse> ingestDiet(@AuthenticationPrincipal UserPrincipal principal,
                                                 @RequestBody @Valid DietIngestRequest request) {
        Long userId = principal.getUserId();
        return Result.success(lifestyleService.ingestDiet(userId, request));
    }

    @PostMapping("/recipes/recommend")
    /**
     * 执行业务操作
     * @param principal 当前登录用户
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<List<RecipeRecommendResponse>> recommendRecipes(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @RequestBody RecipeRecommendRequest request) {
        Long userId = principal.getUserId();
        return Result.success(lifestyleService.recommendRecipes(userId, request));
    }

    @GetMapping("/diet/report/weekly")
    public Result<String> weeklyDiet(@AuthenticationPrincipal UserPrincipal principal,
                                     @RequestParam(value = "familyId", required = false) Long familyId,
                                     @RequestParam(value = "dp", required = false) Boolean dp,
                                     @RequestParam(value = "epsilon", required = false) Double epsilon) {
        Long userId = principal.getUserId();
        return Result.success(lifestyleService.analyzeDietWeekly(userId, familyId, dp, epsilon));
    }

    @PostMapping("/exercise/record")
    /**
     * 执行业务操作
     * @param principal 当前登录用户
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<Void> recordExercise(@AuthenticationPrincipal UserPrincipal principal,
                                       @RequestBody @Valid ExerciseRecordRequest request) {
        Long userId = principal.getUserId();
        lifestyleService.recordExercise(userId, request);
        return Result.success();
    }

    @GetMapping("/exercise/suggest")
    /**
     * 执行业务操作
     * @param principal 当前登录用户
     * @return 业务返回结果
     */
    public Result<String> suggestExercise(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getUserId();
        return Result.success(lifestyleService.suggestExercise(userId));
    }

    @PostMapping("/sleep/record")
    /**
     * 执行业务操作
     * @param principal 当前登录用户
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<Void> recordSleep(@AuthenticationPrincipal UserPrincipal principal,
                                    @RequestBody @Valid SleepRecordRequest request) {
        Long userId = principal.getUserId();
        lifestyleService.recordSleep(userId, request);
        return Result.success();
    }

    @PostMapping("/mood/record")
    /**
     * 执行业务操作
     * @param principal 当前登录用户
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<Void> recordMood(@AuthenticationPrincipal UserPrincipal principal,
                                   @RequestBody @Valid MoodRecordRequest request) {
        Long userId = principal.getUserId();
        lifestyleService.recordMood(userId, request);
        return Result.success();
    }

    @PostMapping("/vitals/record")
    /**
     * 执行业务操作
     * @param principal 当前登录用户
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<Void> recordVitals(@AuthenticationPrincipal UserPrincipal principal,
                                     @RequestBody @Valid VitalsRecordRequest request) {
        Long userId = principal.getUserId();
        lifestyleService.recordVitals(userId, request);
        return Result.success();
    }

    @GetMapping("/sleep/analyze")
    /**
     * 执行业务操作
     * @param principal 当前登录用户
     * @return 业务返回结果
     */
    public Result<String> analyzeSleep(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getUserId();
        return Result.success(lifestyleService.analyzeSleep(userId));
    }
}
