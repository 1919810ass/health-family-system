package com.healthfamily.service;

import com.healthfamily.web.dto.FamilyDashboardResponse;

public interface CollaborationService {
    FamilyDashboardResponse getFamilyDashboard(Long requesterId, Long familyId);
    com.healthfamily.web.dto.HomeAbnormalTodayResponse getAbnormalToday(Long requesterId, Long familyId);
    com.healthfamily.web.dto.HomeHealthIndexResponse getHealthIndex(Long requesterId, Long familyId);
    com.healthfamily.web.dto.HomeTrendResponse getMetricTrend(Long requesterId, Long familyId, String metric, String period);
    com.healthfamily.web.dto.HomeStatusDistributionResponse getStatusDistribution(Long requesterId, Long familyId);
    com.healthfamily.web.dto.HomeEventsResponse getRecentEvents(Long requesterId, Long familyId);
    
    void sendInteraction(Long requesterId, com.healthfamily.web.dto.SendInteractionRequest request);
    java.util.List<com.healthfamily.web.dto.FamilyInteractionDto> getRecentInteractions(Long requesterId, Long familyId);
}
