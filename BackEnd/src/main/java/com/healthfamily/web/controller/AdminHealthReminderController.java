package com.healthfamily.web.controller;

import com.healthfamily.domain.entity.HealthReminderTemplate;
import com.healthfamily.service.AdminHealthReminderService;
import com.healthfamily.web.dto.HealthReminderTemplateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reminders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminHealthReminderController {

    private final AdminHealthReminderService reminderService;

    @GetMapping
    public ResponseEntity<Page<HealthReminderTemplateDto>> getReminderTemplates(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String content) {
        Page<HealthReminderTemplate> templatePage = reminderService.getTemplates(page, size, category, content);
        Page<HealthReminderTemplateDto> dtoPage = templatePage.map(HealthReminderTemplateDto::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping
    public ResponseEntity<HealthReminderTemplateDto> createReminderTemplate(@RequestBody HealthReminderTemplateDto dto) {
        HealthReminderTemplate newTemplate = reminderService.createTemplate(dto.toEntity());
        return ResponseEntity.ok(HealthReminderTemplateDto.fromEntity(newTemplate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HealthReminderTemplateDto> updateReminderTemplate(@PathVariable Long id, @RequestBody HealthReminderTemplateDto dto) {
        HealthReminderTemplate updatedTemplate = reminderService.updateTemplate(id, dto.toEntity());
        return ResponseEntity.ok(HealthReminderTemplateDto.fromEntity(updatedTemplate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminderTemplate(@PathVariable Long id) {
        reminderService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
