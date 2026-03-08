package com.healthfamily.service;

import com.healthfamily.domain.entity.HealthReminderTemplate;
import com.healthfamily.domain.repository.HealthReminderRepository;
import com.healthfamily.domain.repository.HealthReminderTemplateRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminHealthReminderService {

    private final HealthReminderTemplateRepository templateRepository;
    private final HealthReminderRepository reminderRepository;

    @Transactional(readOnly = true)
    public Page<HealthReminderTemplate> getTemplates(int page, int size, String category, String content) {
        Specification<HealthReminderTemplate> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (StringUtils.hasText(content)) {
                predicates.add(cb.like(root.get("content"), "%" + content + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<HealthReminderTemplate> templatePage = templateRepository.findAll(spec, pageable);

        // 动态计算每个模板的使用人数
        templatePage.getContent().forEach(template -> {
            long count = reminderRepository.countByContent(template.getContent());
            template.setUserCount((int) count);
        });

        return templatePage;
    }

    public HealthReminderTemplate createTemplate(HealthReminderTemplate template) {
        // Reset fields that should not be set by client
        template.setId(null);
        template.setUserCount(0);
        return templateRepository.save(template);
    }

    public HealthReminderTemplate updateTemplate(Long id, HealthReminderTemplate details) {
        HealthReminderTemplate existingTemplate = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));

        existingTemplate.setContent(details.getContent());
        existingTemplate.setCategory(details.getCategory());
        existingTemplate.setStatus(details.getStatus());
        // userCount and createdAt should not be updated manually

        return templateRepository.save(existingTemplate);
    }

    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new RuntimeException("Template not found with id: " + id);
        }
        templateRepository.deleteById(id);
    }
}
