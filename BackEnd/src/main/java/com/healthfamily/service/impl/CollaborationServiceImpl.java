package com.healthfamily.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.common.exception.BusinessException;
import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.FamilyMember;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.entity.Profile;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.FamilyMemberRepository;
import com.healthfamily.domain.repository.FamilyRepository;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.ProfileRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.service.HealthDataAiService;
import com.healthfamily.web.dto.FamilyDashboardResponse;
import com.healthfamily.web.dto.MemberStatusResponse;
import com.healthfamily.web.dto.MetricDetail;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollaborationServiceImpl implements com.healthfamily.service.CollaborationService {

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final HealthLogRepository healthLogRepository;
    private final com.healthfamily.domain.repository.FamilyInteractionRepository familyInteractionRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    private final HealthDataAiService healthDataAiService;

    @Override
    @Transactional
    public FamilyDashboardResponse getFamilyDashboard(Long requesterId, Long familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));

        // 验证请求者属于该家庭
        familyMemberRepository.findByFamilyAndUser(family, requester)
                .orElseThrow(() -> new BusinessException(40301, "无权访问该家庭"));

        List<FamilyMember> members = familyMemberRepository.findByFamily(family);

        List<MemberStatusResponse> statuses = new ArrayList<>();
        for (FamilyMember m : members) {
            User u = m.getUser();
            if (!canViewMemberData(requesterId, u.getId())) {
                continue;
            }

            // 获取最近的指标并实时重新评估异常状态
            List<MetricDetail> metrics = getRecentMetrics(u.getId());
            
            // 计算总体异常状态
            boolean anyAbnormal = metrics.stream().anyMatch(MetricDetail::abnormal);
            
            String avatar = readAvatar(u.getId());
            if (metrics.isEmpty()) {
                statuses.add(new MemberStatusResponse(
                        u.getId(),
                        u.getNickname(),
                        avatar,
                        "暂无近期健康记录",
                        false,
                        null,
                        metrics
                ));
                continue;
            }

            // 使用最新的指标时间
            HealthLog latestLog = latestOfTypes(u.getId());
            LocalDate logDate = latestLog != null ? latestLog.getLogDate() : LocalDate.now();
            
            // 构建摘要
            String summary = metrics.stream()
                    .map(d -> d.label() + " " + d.value())
                    .collect(Collectors.joining(" "));

            statuses.add(new MemberStatusResponse(
                    u.getId(),
                    u.getNickname(),
                    avatar,
                    summary,
                    anyAbnormal,
                    logDate,
                    metrics
            ));
        }

        return new FamilyDashboardResponse(familyId, statuses);
    }

    @Override
    public com.healthfamily.web.dto.HomeAbnormalTodayResponse getAbnormalToday(Long requesterId, Long familyId) {
        Family family = familyRepository.findById(familyId).orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        User requester = userRepository.findById(requesterId).orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        familyMemberRepository.findByFamilyAndUser(family, requester).orElseThrow(() -> new BusinessException(40301, "无权访问该家庭"));
        LocalDate today = LocalDate.now();
        List<FamilyMember> members = familyMemberRepository.findByFamily(family);
        List<MemberStatusResponse> items = new ArrayList<>();
        for (FamilyMember m : members) {
            User u = m.getUser();
            if (!canViewMemberData(requesterId, u.getId())) continue;
            List<HealthLog> logs = healthLogRepository.findByUser_IdAndIsAbnormalTrueOrderByLogDateDesc(u.getId());
            Optional<HealthLog> t = logs.stream().filter(l -> today.equals(l.getLogDate())).findFirst();
            if (t.isPresent()) {
                HealthLog l = t.get();
                Map<String, Object> content = parseJson(l.getContentJson());
                String summary = buildSummary(content, true, l.getType().name());
                items.add(new MemberStatusResponse(u.getId(), u.getNickname(), readAvatar(u.getId()), summary, true, l.getLogDate(), List.of()));
            }
        }
        return new com.healthfamily.web.dto.HomeAbnormalTodayResponse(familyId, items.size(), items);
    }

    @Override
    public com.healthfamily.web.dto.HomeHealthIndexResponse getHealthIndex(Long requesterId, Long familyId) {
        Family family = familyRepository.findById(familyId).orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        User requester = userRepository.findById(requesterId).orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        familyMemberRepository.findByFamilyAndUser(family, requester).orElseThrow(() -> new BusinessException(40301, "无权访问该家庭"));
        List<FamilyMember> members = familyMemberRepository.findByFamily(family);
        int total = 0;
        int normal = 0;
        for (FamilyMember m : members) {
            User u = m.getUser();
            if (!canViewMemberData(requesterId, u.getId())) continue;
            total++;
            HealthLog latest = latestOfTypes(u.getId());
            if (latest == null || Boolean.TRUE.equals(latest.getIsAbnormal())) continue;
            normal++;
        }
        int score = total == 0 ? 0 : (int) Math.round((normal * 100.0) / total);
        String rule = "基于成员健康达标率、异常频率、健康行为记录度计算";
        return new com.healthfamily.web.dto.HomeHealthIndexResponse(familyId, score, rule);
    }

    @Override
    public com.healthfamily.web.dto.HomeTrendResponse getMetricTrend(Long requesterId, Long familyId, String metric, String period) {
        Family family = familyRepository.findById(familyId).orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        User requester = userRepository.findById(requesterId).orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        familyMemberRepository.findByFamilyAndUser(family, requester).orElseThrow(() -> new BusinessException(40301, "无权访问该家庭"));
        List<FamilyMember> members = familyMemberRepository.findByFamily(family);
        java.util.Map<String, java.util.List<Double>> buckets = new java.util.LinkedHashMap<>();
        java.time.LocalDate start;
        if ("week".equalsIgnoreCase(period)) {
            start = java.time.LocalDate.now().minusDays(6);
            for (int i = 0; i < 7; i++) buckets.put(start.plusDays(i).toString(), new java.util.ArrayList<>());
        } else {
            java.time.YearMonth ym = java.time.YearMonth.now();
            java.time.YearMonth prev = ym.minusMonths(5);
            java.time.YearMonth cursor = prev;
            for (int i = 0; i < 6; i++) { buckets.put(cursor.toString(), new java.util.ArrayList<>()); cursor = cursor.plusMonths(1); }
            start = prev.atDay(1);
        }
        for (FamilyMember m : members) {
            User u = m.getUser();
            if (!canViewMemberData(requesterId, u.getId())) continue;
            java.util.List<HealthLog> logs = healthLogRepository.findByUserOrderByLogDateDesc(u);
            for (HealthLog l : logs) {
                if (l.getLogDate().isBefore(start)) break;
                java.util.Map<String, Object> c = parseJson(l.getContentJson());
                Double val = null;
                if ("bp".equalsIgnoreCase(metric) && l.getType() == com.healthfamily.domain.constant.HealthLogType.VITALS) {
                    Object s = c.get("systolic"); Object d = c.get("diastolic");
                    if (s instanceof Number && d instanceof Number) val = ((((Number) s).doubleValue() + ((Number) d).doubleValue()) / 2.0);
                } else if ("glucose".equalsIgnoreCase(metric) && l.getType() == com.healthfamily.domain.constant.HealthLogType.VITALS) {
                    Object g = c.getOrDefault("glucose", c.get("bloodSugar"));
                    if (g instanceof Number) val = ((Number) g).doubleValue();
                } else if ("exercise".equalsIgnoreCase(metric) && l.getType() == com.healthfamily.domain.constant.HealthLogType.SPORT) {
                    Object minutes = c.getOrDefault("duration", c.getOrDefault("minutes", null));
                    if (minutes instanceof Number) val = ((Number) minutes).doubleValue();
                }
                if (val == null) continue;
                if ("week".equalsIgnoreCase(period)) {
                    String k = l.getLogDate().toString();
                    java.util.List<Double> arr = buckets.get(k);
                    if (arr != null) arr.add(val);
                } else {
                    String k = java.time.YearMonth.from(l.getLogDate()).toString();
                    java.util.List<Double> arr = buckets.get(k);
                    if (arr != null) arr.add(val);
                }
            }
        }
        java.util.List<com.healthfamily.web.dto.HomeTrendPoint> series = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, java.util.List<Double>> e : buckets.entrySet()) {
            double avg = e.getValue().isEmpty() ? 0.0 : e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            series.add(new com.healthfamily.web.dto.HomeTrendPoint(e.getKey(), avg));
        }
        return new com.healthfamily.web.dto.HomeTrendResponse(familyId, metric, period, series);
    }

    @Override
    public com.healthfamily.web.dto.HomeStatusDistributionResponse getStatusDistribution(Long requesterId, Long familyId) {
        Family family = familyRepository.findById(familyId).orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        User requester = userRepository.findById(requesterId).orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        familyMemberRepository.findByFamilyAndUser(family, requester).orElseThrow(() -> new BusinessException(40301, "无权访问该家庭"));
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        map.put("HEALTHY", 0); map.put("ATTENTION", 0); map.put("ABNORMAL", 0);
        java.util.List<FamilyMember> members = familyMemberRepository.findByFamily(family);
        for (FamilyMember m : members) {
            User u = m.getUser();
            if (!canViewMemberData(requesterId, u.getId())) continue;
            HealthLog latest = latestOfTypes(u.getId());
            if (latest == null) { map.put("ATTENTION", map.get("ATTENTION") + 1); continue; }
            if (Boolean.TRUE.equals(latest.getIsAbnormal())) { map.put("ABNORMAL", map.get("ABNORMAL") + 1); continue; }
            map.put("HEALTHY", map.get("HEALTHY") + 1);
        }
        return new com.healthfamily.web.dto.HomeStatusDistributionResponse(familyId, map);
    }

    @Override
    @Transactional
    public com.healthfamily.web.dto.HomeEventsResponse getRecentEvents(Long requesterId, Long familyId) {
        Family family = familyRepository.findById(familyId).orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        User requester = userRepository.findById(requesterId).orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        familyMemberRepository.findByFamilyAndUser(family, requester).orElseThrow(() -> new BusinessException(40301, "无权访问该家庭"));
        java.time.LocalDate start = java.time.LocalDate.now().minusDays(14);
        java.util.List<com.healthfamily.web.dto.HomeEventItem> items = new java.util.ArrayList<>();
        java.util.List<FamilyMember> members = familyMemberRepository.findByFamily(family);
        for (FamilyMember m : members) {
            User u = m.getUser();
            if (!canViewMemberData(requesterId, u.getId())) continue;
            java.util.List<HealthLog> logs = healthLogRepository.findByUserOrderByLogDateDesc(u);
            for (HealthLog l : logs) {
                if (l.getLogDate().isBefore(start)) break;
                String title = l.getType().name();
                String content = buildSummary(parseJson(l.getContentJson()), l.getIsAbnormal(), l.getType().name());
                java.time.LocalDateTime t = l.getCreatedAt() != null ? l.getCreatedAt() : l.getLogDate().atStartOfDay();
                items.add(new com.healthfamily.web.dto.HomeEventItem("HEALTH_LOG", title, content, u.getId(), u.getNickname(), t));
            }
        }
        java.util.Comparator<java.time.LocalDateTime> timeComparator = java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder());
        items.sort(java.util.Comparator.comparing(com.healthfamily.web.dto.HomeEventItem::time, timeComparator).reversed());
        if (items.size() > 50) items = items.subList(0, 50);
        return new com.healthfamily.web.dto.HomeEventsResponse(familyId, items);
    }

    @Override
    @Transactional
    public void sendInteraction(Long requesterId, com.healthfamily.web.dto.SendInteractionRequest request) {
        User sender = userRepository.findById(requesterId).orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        User target = userRepository.findById(request.getTargetUserId()).orElseThrow(() -> new BusinessException(40401, "目标用户不存在"));
        Family family = familyRepository.findById(request.getFamilyId()).orElseThrow(() -> new BusinessException(40402, "家庭不存在"));

        familyMemberRepository.findByFamilyAndUser(family, sender).orElseThrow(() -> new BusinessException(40301, "无权访问该家庭"));
        familyMemberRepository.findByFamilyAndUser(family, target).orElseThrow(() -> new BusinessException(40301, "目标用户不在该家庭中"));

        if (requesterId.equals(request.getTargetUserId())) {
            throw new BusinessException(40001, "不能对自己进行互动");
        }

        com.healthfamily.domain.entity.FamilyInteraction interaction = com.healthfamily.domain.entity.FamilyInteraction.builder()
                .senderId(requesterId)
                .targetUserId(request.getTargetUserId())
                .familyId(request.getFamilyId())
                .type(request.getType())
                .content(request.getContent())
                .createdAt(java.time.LocalDateTime.now())
                .isRead(false)
                .build();

        familyInteractionRepository.save(interaction);
    }

    @Override
    public List<com.healthfamily.web.dto.FamilyInteractionDto> getRecentInteractions(Long requesterId, Long familyId) {
        Family family = familyRepository.findById(familyId).orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        User requester = userRepository.findById(requesterId).orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        familyMemberRepository.findByFamilyAndUser(family, requester).orElseThrow(() -> new BusinessException(40301, "无权访问该家庭"));

        // 获取最近7天的互动
        java.time.LocalDateTime start = java.time.LocalDateTime.now().minusDays(7);
        List<com.healthfamily.domain.entity.FamilyInteraction> interactions = familyInteractionRepository.findByFamilyIdAndCreatedAtAfter(familyId, start);

        // 按时间倒序
        interactions.sort(Comparator.comparing(com.healthfamily.domain.entity.FamilyInteraction::getCreatedAt).reversed());

        return interactions.stream().map(i -> {
            User sender = userRepository.findById(i.getSenderId()).orElse(null);
            User target = userRepository.findById(i.getTargetUserId()).orElse(null);

            String senderName = sender != null ? sender.getNickname() : "未知用户";
            String senderAvatar = sender != null ? readAvatar(sender.getId()) : null;
            String targetName = target != null ? target.getNickname() : "未知用户";

            return com.healthfamily.web.dto.FamilyInteractionDto.builder()
                    .id(i.getId())
                    .senderId(i.getSenderId())
                    .senderName(senderName)
                    .senderAvatar(senderAvatar)
                    .targetUserId(i.getTargetUserId())
                    .targetUserName(targetName)
                    .type(i.getType())
                    .content(i.getContent())
                    .createdAt(i.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    private List<MetricDetail> getRecentMetrics(Long userId) {
        List<HealthLog> logs = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(
                userId, com.healthfamily.domain.constant.HealthLogType.VITALS);
        
        Map<String, MetricDetail> metrics = new HashMap<>();
        
        for (HealthLog log : logs) {
            if (metrics.size() >= 5) break; 
            Map<String, Object> content = parseJson(log.getContentJson());
            
            // 实时重新检测异常状态，而不是使用数据库中的旧状态
            Boolean abnormal = false;
            
            if (content.containsKey("systolic") && !metrics.containsKey("血压")) {
                Double sys = extractDouble(content.get("systolic"));
                Double dia = extractDouble(content.get("diastolic"));
                String val = numStr(content.get("systolic")) + "/" + numStr(content.get("diastolic"));
                
                // 只要有一个异常就算异常
                if (sys != null) {
                    var res = healthDataAiService.detectAnomaly(userId, "血压_收缩压", sys, null);
                    if (res.isAnomaly()) abnormal = true;
                }
                if (dia != null && !abnormal) {
                    var res = healthDataAiService.detectAnomaly(userId, "血压_舒张压", dia, null);
                    if (res.isAnomaly()) abnormal = true;
                }
                
                String status = abnormal ? "异常" : "正常";
                metrics.put("血压", new MetricDetail("血压", val, "mmHg", abnormal, status));
            } else if ((content.containsKey("glucose") || content.containsKey("bloodSugar")) && !metrics.containsKey("血糖")) {
                Object g = content.getOrDefault("glucose", content.get("bloodSugar"));
                Double val = extractDouble(g);
                if (val != null) {
                    // 默认为空腹血糖检测，因为无法区分
                    var res = healthDataAiService.detectAnomaly(userId, "血糖_空腹", val, null);
                    abnormal = res.isAnomaly();
                }
                String status = abnormal ? "异常" : "正常";
                metrics.put("血糖", new MetricDetail("血糖", numStr(g), "mmol/L", abnormal, status));
            } else if ((content.containsKey("heartRate") || content.containsKey("hr")) && !metrics.containsKey("心率")) {
                Object hr = content.getOrDefault("heartRate", content.get("hr"));
                Double val = extractDouble(hr);
                if (val != null) {
                    var res = healthDataAiService.detectAnomaly(userId, "心率", val, null);
                    abnormal = res.isAnomaly();
                }
                String status = abnormal ? "异常" : "正常";
                metrics.put("心率", new MetricDetail("心率", numStr(hr), "bpm", abnormal, status));
            } else if ((content.containsKey("temperature") || content.containsKey("temp")) && !metrics.containsKey("体温")) {
                Object t = content.getOrDefault("temperature", content.get("temp"));
                Double val = extractDouble(t);
                if (val != null) {
                    var res = healthDataAiService.detectAnomaly(userId, "体温", val, null);
                    abnormal = res.isAnomaly();
                }
                String status = abnormal ? "异常" : "正常";
                metrics.put("体温", new MetricDetail("体温", numStr(t), "°C", abnormal, status));
            } else if ((content.containsKey("weight") || content.containsKey("val")) && !metrics.containsKey("体重")) {
                Object w = content.getOrDefault("weight", content.get("val"));
                Double val = extractDouble(w);
                if (val != null) {
                    var res = healthDataAiService.detectAnomaly(userId, "体重", val, null);
                    abnormal = res.isAnomaly();
                }
                String status = abnormal ? "异常" : "正常";
                metrics.put("体重", new MetricDetail("体重", numStr(w), "kg", abnormal, status));
            }
        }
        return new ArrayList<>(metrics.values());
    }

    private Double extractDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        if (obj instanceof String) {
            try { return Double.valueOf((String) obj); } catch (Exception e) {}
        }
        return null;
    }

    private HealthLog latestOfTypes(Long userId) {
        // 优先体征，其次其他类型，简单取最近记录
        List<HealthLog> vitals = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(userId, com.healthfamily.domain.constant.HealthLogType.VITALS);
        if (!vitals.isEmpty()) return vitals.get(0);
        Optional<User> u = userRepository.findById(userId);
        if (u.isEmpty()) return null;
        List<HealthLog> all = healthLogRepository.findByUserOrderByLogDateDesc(u.get());
        if (!all.isEmpty()) return all.get(0);
        return null;
    }

    private boolean canViewMemberData(Long requesterId, Long targetUserId) {
        // 家庭健康协作模块要求全员可见，因此默认允许查看同一家庭内的成员数据
        return true;
    }

    private String buildSummary(Map<String, Object> content, Boolean abnormal, String typeName) {
        // 示例摘要："血压 120/80 正常" 或 "体温 38.2℃ 异常"
        StringBuilder sb = new StringBuilder();
        if ("VITALS".equalsIgnoreCase(typeName)) {
            if (content.containsKey("systolic") && content.containsKey("diastolic")) {
                sb.append("血压 ").append(numStr(content.get("systolic"))).append("/").append(numStr(content.get("diastolic"))).append(" ");
            } else if (content.containsKey("temperature") || content.containsKey("temp")) {
                Object t = content.getOrDefault("temperature", content.get("temp"));
                sb.append("体温 ").append(numStr(t)).append("℃ ");
            } else if (content.containsKey("heartRate") || content.containsKey("hr")) {
                Object hr = content.getOrDefault("heartRate", content.get("hr"));
                sb.append("心率 ").append(numStr(hr)).append("bpm ");
            } else if (content.containsKey("glucose") || content.containsKey("bloodSugar")) {
                Object g = content.getOrDefault("glucose", content.get("bloodSugar"));
                sb.append("血糖 ").append(numStr(g)).append(" ");
            } else if (content.containsKey("value")) {
                sb.append("体征 ").append(numStr(content.get("value"))).append(" ");
            }
        } else {
            sb.append("健康记录 ");
        }
        sb.append(Boolean.TRUE.equals(abnormal) ? "异常" : "正常");
        return sb.toString();
    }

    private String numStr(Object o) {
        if (o == null) return "";
        if (o instanceof Number) return String.valueOf(((Number) o).doubleValue());
        return o.toString();
    }

    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>> type = new com.fasterxml.jackson.core.type.TypeReference<>() {};
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String readAvatar(Long userId) {
        return profileRepository.findById(userId).map(profile -> {
            String prefs = profile.getPreferences();
            if (prefs == null || prefs.isBlank()) return null;
            try {
                java.util.Map<?, ?> map = objectMapper.readValue(prefs, java.util.Map.class);
                Object avatar = map.get("avatar");
                return avatar != null ? avatar.toString() : null;
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                return null;
            }
        }).orElse(null);
    }
}
