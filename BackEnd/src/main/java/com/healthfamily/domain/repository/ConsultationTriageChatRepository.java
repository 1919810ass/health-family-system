package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.ConsultationTriageChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationTriageChatRepository extends JpaRepository<ConsultationTriageChat, Long> {
    
    List<ConsultationTriageChat> findBySessionIdOrderByGmtCreateAsc(Long sessionId);
}
