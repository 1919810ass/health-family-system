package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.HealthReminderTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HealthReminderTemplateRepository extends JpaRepository<HealthReminderTemplate, Long>, JpaSpecificationExecutor<HealthReminderTemplate> {
}
