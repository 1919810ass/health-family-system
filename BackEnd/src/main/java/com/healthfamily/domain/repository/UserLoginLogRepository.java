package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.UserLoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserLoginLogRepository extends JpaRepository<UserLoginLog, Long> {
    
    @Query("SELECT log FROM UserLoginLog log WHERE log.loginTime BETWEEN :startTime AND :endTime ORDER BY log.loginTime DESC")
    Page<UserLoginLog> findByLoginTimeBetweenOrderByLoginTimeDesc(@Param("startTime") LocalDateTime startTime, 
                                                                 @Param("endTime") LocalDateTime endTime, 
                                                                 Pageable pageable);

    @Query("SELECT log FROM UserLoginLog log WHERE log.loginTime BETWEEN :startTime AND :endTime ORDER BY log.loginTime DESC")
    List<UserLoginLog> findByLoginTimeBetweenOrderByLoginTimeDesc(@Param("startTime") LocalDateTime startTime,
                                                                  @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(DISTINCT log.userId) FROM UserLoginLog log WHERE log.loginTime BETWEEN :startTime AND :endTime")
    long countDistinctUserIdByLoginTimeBetween(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(log) FROM UserLoginLog log WHERE log.loginTime BETWEEN :startTime AND :endTime")
    long countByLoginTimeBetween(@Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT log FROM UserLoginLog log WHERE log.userId = :userId ORDER BY log.loginTime DESC")
    List<UserLoginLog> findByUserIdOrderByLoginTimeDesc(@Param("userId") Long userId);
    
    @Query("SELECT log FROM UserLoginLog log ORDER BY log.loginTime DESC")
    Page<UserLoginLog> findAllOrderByLoginTimeDesc(Pageable pageable);
}
