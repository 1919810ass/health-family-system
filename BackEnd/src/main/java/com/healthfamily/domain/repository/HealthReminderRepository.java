package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.ReminderStatus;
import com.healthfamily.domain.entity.HealthReminder;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
/**
 * 健康提醒数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.List;

public interface HealthReminderRepository extends JpaRepository<HealthReminder, Long> {

    List<HealthReminder> findByUserOrderByScheduledTimeDesc(User user);

    List<HealthReminder> findByUserAndStatusOrderByScheduledTimeAsc(User user, ReminderStatus status);

    List<HealthReminder> findByStatusAndScheduledTimeLessThanEqual(ReminderStatus status, LocalDateTime time);

    List<HealthReminder> findByUser_IdAndStatus(Long userId, ReminderStatus status);

    List<HealthReminder> findByFamily_IdOrderByScheduledTimeAsc(Long familyId);

    List<HealthReminder> findByAssignedTo_IdOrderByScheduledTimeAsc(Long userId);

    List<HealthReminder> findByUserAndTypeOrderByScheduledTimeDesc(User user, com.healthfamily.domain.constant.ReminderType type);

    List<HealthReminder> findByUser_IdOrAssignedTo_IdOrderByScheduledTimeAsc(Long userId, Long assignedToId);
    
    List<HealthReminder> findByCreator_Id(Long creatorId);
    
    List<HealthReminder> findByCreator_IdAndFamily_Id(Long creatorId, Long familyId);
}

