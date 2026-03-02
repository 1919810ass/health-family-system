package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.FollowUpTask;
import com.healthfamily.domain.entity.HealthPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowUpTaskRepository extends JpaRepository<FollowUpTask, Long> {

    /**
     * 根据健康计划查找所有关联的随访任务
     * @param healthPlan 健康计划实体
     * @return 任务列表
     */
    List<FollowUpTask> findByHealthPlan(HealthPlan healthPlan);

    /**
     * 根据ID列表批量删除任务
     * Spring Data JPA 会自动实现此方法
     * @param ids 要删除的任务ID列表
     */
    void deleteAllByIdIn(List<Long> ids);
}
