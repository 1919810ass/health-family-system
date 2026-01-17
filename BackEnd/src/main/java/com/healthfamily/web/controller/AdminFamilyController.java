package com.healthfamily.web.controller;

import com.healthfamily.annotation.Audit;
import com.healthfamily.domain.constant.SensitivityLevel;
import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.service.FamilyService;
import com.healthfamily.web.dto.AdminFamilyUpsertRequest;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
/**
 * 管理员家庭管理控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/families")
public class AdminFamilyController {

    private final FamilyService familyService;
    private final UserRepository userRepository;

    /**
     * 获取家庭列表
     */
    @GetMapping
    public Result<?> getFamilies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        // 调用服务层获取家庭列表
        Map<String, Object> result = familyService.getAdminFamilyList(keyword, status, page, size, startTime, endTime);
        return Result.success(result);
    }

    /**
     * 根据ID获取家庭详情
     */
    @GetMapping("/{id}")
    public Result<Family> getFamilyById(@PathVariable Long id) {
        Family family = familyService.findById(id);
        if (family == null) {
            return Result.error(404, "家庭不存在");
        }
        return Result.success(family);
    }

    /**
     * 创建家庭
     */
    @PostMapping
    @Audit(action = "CREATE_FAMILY", resource = "Family", sensitivity = SensitivityLevel.HIGH)
    public Result<Family> createFamily(@RequestBody AdminFamilyUpsertRequest request) {
        if (request == null) return Result.error(400, "请求体不能为空");
        if (request.ownerId() == null) return Result.error(400, "缺少ownerId");
        if (request.name() == null || request.name().isBlank()) return Result.error(400, "缺少name");
        User owner = userRepository.findById(request.ownerId()).orElse(null);
        if (owner == null) return Result.error(404, "所有者不存在");

        Family family = Family.builder()
                .owner(owner)
                .name(request.name())
                .status(request.status() != null ? request.status() : 1)
                .build();
        Family createdFamily = familyService.create(family);
        return Result.success(createdFamily);
    }

    /**
     * 更新家庭信息
     */
    @PutMapping("/{id}")
    @Audit(action = "UPDATE_FAMILY", resource = "Family", sensitivity = SensitivityLevel.HIGH)
    public Result<Family> updateFamily(@PathVariable Long id, @RequestBody AdminFamilyUpsertRequest request) {
        Family family = Family.builder()
                .name(request != null ? request.name() : null)
                .status(request != null ? request.status() : null)
                .build();
        Family updatedFamily = familyService.update(id, family);
        if (updatedFamily == null) {
            return Result.error(404, "家庭不存在");
        }
        return Result.success(updatedFamily);
    }

    /**
     * 删除家庭
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteFamily(@PathVariable Long id) {
        boolean deleted = familyService.deleteById(id);
        if (!deleted) {
            return Result.error(404, "家庭不存在");
        }
        return Result.success("删除成功");
    }

    /**
     * 更新家庭状态
     */
    @PutMapping("/{id}/status")
    @Audit(action = "UPDATE_FAMILY_STATUS", resource = "Family", sensitivity = SensitivityLevel.HIGH)
    public Result<?> updateFamilyStatus(@PathVariable Long id,
                                        @RequestParam(required = false) String status,
                                        @RequestBody(required = false) Map<String, Object> body) {
        String resolved = status;
        if ((resolved == null || resolved.isBlank()) && body != null && body.get("status") != null) {
            resolved = String.valueOf(body.get("status"));
        }
        if (resolved == null || resolved.isBlank()) {
            return Result.error(400, "缺少status参数");
        }
        boolean updated = familyService.updateStatus(id, resolved);
        if (!updated) {
            return Result.error(404, "家庭不存在");
        }
        return Result.success("状态更新成功");
    }
}
