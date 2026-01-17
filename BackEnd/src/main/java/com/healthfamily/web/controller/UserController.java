package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.UserService;
import com.healthfamily.web.dto.ChangePasswordRequest;
import com.healthfamily.web.dto.HealthProfileResponse;
import com.healthfamily.web.dto.ProfileHealthUpdateRequest;
import com.healthfamily.web.dto.Result;
import com.healthfamily.web.dto.UpdateNotificationsRequest;
import com.healthfamily.web.dto.UpdateProfileRequest;
import com.healthfamily.web.dto.UserProfileResponse;
import com.healthfamily.web.dto.FamilyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @org.springframework.beans.factory.annotation.Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping("/profile")
    public Result<UserProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                  @RequestHeader(value = "X-User-Id", required = false) Long userHeader) {
        Long userId = resolveUserId(principal, userHeader, "getProfile");
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<UserProfileResponse> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                     @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                     @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = resolveUserId(principal, userHeader, "updateProfile");
        log.info("updateProfile userId={} payload={}", userId, request);
        return Result.success(userService.updateProfile(userId, request));
    }

    @GetMapping("/profile/health")
    public Result<HealthProfileResponse> getHealthProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                          @RequestHeader(value = "X-User-Id", required = false) Long userHeader) {
        Long userId = resolveUserId(principal, userHeader, "getHealthProfile");
        return Result.success(userService.getHealthProfile(userId));
    }

    @PutMapping("/profile/health")
    public Result<UserProfileResponse> updateHealthProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                           @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                           @Valid @RequestBody ProfileHealthUpdateRequest request) {
        Long userId = resolveUserId(principal, userHeader, "updateHealthProfile");
        log.info("updateHealthProfile userId={} payload={}", userId, request);
        return Result.success(userService.updateHealthProfile(userId, request));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<AvatarResponse> updateAvatar(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                               @RequestPart("file") MultipartFile file) {
        Long userId = resolveUserId(principal, userHeader, "updateAvatar");
        log.info("updateAvatar userId={} filename={} size={}", userId, file.getOriginalFilename(), file.getSize());
        String url = userService.updateAvatar(userId, file);
        log.info("updateAvatar saved url={}", url);
        return Result.success(new AvatarResponse(url));
    }

    @GetMapping("/avatar/{userId}/{filename}")
    public ResponseEntity<org.springframework.core.io.Resource> getAvatar(@PathVariable("userId") Long userId,
                                                        @PathVariable("filename") String filename) {
        log.info("getAvatar userId={} filename={}", userId, filename);
        // 使用配置的上传目录
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

        // 文件不存在，返回默认头像
        log.warn("Avatar file not found: {}, returning default.", path);
        org.springframework.core.io.Resource defaultAvatar = new org.springframework.core.io.ClassPathResource("static/default_avatar.svg");
        if (defaultAvatar.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/svg+xml"))
                    .body(defaultAvatar);
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/password")
    public Result<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                       @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = resolveUserId(principal, userHeader, "changePassword");
        log.info("changePassword userId={}", userId);
        userService.changePassword(userId, request);
        return Result.success();
    }

    @PutMapping("/notifications")
    public Result<UpdateNotificationsRequest> updateNotifications(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                                  @RequestBody UpdateNotificationsRequest request) {
        Long userId = resolveUserId(principal, userHeader, "updateNotifications");
        log.info("updateNotifications userId={} payload={}", userId, request);
        return Result.success(userService.updateNotifications(userId, request));
    }

    @GetMapping("/families")
    public Result<java.util.List<FamilyResponse>> listFamilies(@AuthenticationPrincipal UserPrincipal principal,
                                                               @RequestHeader(value = "X-User-Id", required = false) Long userHeader) {
        Long userId = resolveUserId(principal, userHeader, "listFamilies");
        return Result.success(userService.listFamilies(userId));
    }

    @PostMapping("/families/{id}/switch")
    public Result<FamilyResponse> switchFamily(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                               @PathVariable("id") Long familyId) {
        Long userId = resolveUserId(principal, userHeader, "switchFamily");
        return Result.success(userService.switchCurrentFamily(userId, familyId));
    }

    public record AvatarResponse(String avatar) {}

    private Long resolveUserId(UserPrincipal principal, Long userHeader, String action) {
        boolean authenticated = principal != null;
        Long resolved = authenticated ? principal.getUserId() : userHeader;
        log.info("authState action={} authenticated={} principalUserId={} headerUserId={}",
                action, authenticated, authenticated ? principal.getUserId() : null, userHeader);
        if (resolved == null) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("未登录或缺少用户身份信息");
        }
        return resolved;
    }
}
