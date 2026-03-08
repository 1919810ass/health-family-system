package com.healthfamily.service.impl;

import com.healthfamily.domain.constant.Sex;
import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.Profile;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.ConstitutionAssessmentRepository;
import com.healthfamily.domain.repository.ProfileRepository;
import com.healthfamily.service.AdminAssessmentService;
import com.healthfamily.web.model.Paging;
import com.healthfamily.web.model.request.AssessmentQueryRequest;
import com.healthfamily.web.model.response.AssessmentResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminAssessmentServiceImpl implements AdminAssessmentService {

    private final ConstitutionAssessmentRepository assessmentRepository;
    private final ProfileRepository profileRepository;

    public AdminAssessmentServiceImpl(ConstitutionAssessmentRepository assessmentRepository,
                                      ProfileRepository profileRepository) {
        this.assessmentRepository = assessmentRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    public Paging<AssessmentResponse> getAssessments(AssessmentQueryRequest request) {
        // 1. 读取全部 ConstitutionAssessment（当前规模下可行，如后续数据量增大可改为分页查询）
        List<ConstitutionAssessment> all = assessmentRepository.findAll();

        // 2. 基于查询条件过滤（用户、体质类型、时间范围）
        List<ConstitutionAssessment> filtered = all.stream()
                .filter(a -> filterByUser(a, request.getUserId()))
                .filter(a -> filterByConstitutionType(a, request.getConstitutionType()))
                .filter(a -> filterByDateRange(a, request.getStartDate(), request.getEndDate()))
                .sorted(Comparator.comparing(ConstitutionAssessment::getCreatedAt).reversed())
                .collect(Collectors.toList());

        // 3. 手动分页
        int page = Math.max(request.getPage(), 1);
        int size = Math.max(request.getSize(), 1);
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, filtered.size());

        List<AssessmentResponse> pageData;
        if (fromIndex >= filtered.size()) {
            pageData = List.of();
        } else {
            pageData = filtered.subList(fromIndex, toIndex).stream()
                    .map(this::toAdminResponse)
                    .collect(Collectors.toList());
        }

        return new Paging<>(pageData, (long) filtered.size());
    }

    @Override
    public Map<String, Long> getAssessmentStats() {
        List<ConstitutionAssessment> all = assessmentRepository.findAll();
        return all.stream()
                .collect(Collectors.groupingBy(
                        a -> mapPrimaryTypeToLabel(a.getPrimaryType()),
                        Collectors.counting()
                ));
    }

    private boolean filterByUser(ConstitutionAssessment assessment, String userKeyword) {
        if (!StringUtils.hasText(userKeyword)) {
            return true;
        }
        String keyword = userKeyword.trim();
        User user = assessment.getUser();
        if (user == null) {
            return false;
        }

        boolean matchId = false;
        boolean matchName = false;

        if (keyword.chars().allMatch(Character::isDigit)) {
            matchId = String.valueOf(user.getId()).contains(keyword);
        }

        String nickname = user.getNickname();
        if (StringUtils.hasText(nickname)) {
            matchName = nickname.contains(keyword);
        }

        return matchId || matchName;
    }

    private boolean filterByConstitutionType(ConstitutionAssessment assessment, String constitutionType) {
        if (!StringUtils.hasText(constitutionType)) {
            return true;
        }
        String label = mapPrimaryTypeToLabel(assessment.getPrimaryType());
        return constitutionType.equals(label);
    }

    private boolean filterByDateRange(ConstitutionAssessment assessment, String startDateStr, String endDateStr) {
        if (!StringUtils.hasText(startDateStr) || !StringUtils.hasText(endDateStr)) {
            return true;
        }
        if (assessment.getCreatedAt() == null) {
            return false;
        }
        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(startDateStr);
            end = LocalDate.parse(endDateStr);
        } catch (Exception e) {
            // 日期解析失败时，不进行过滤，避免前端因为单个错误而看不到数据
            return true;
        }
        LocalDate createdDate = assessment.getCreatedAt().toLocalDate();
        return !createdDate.isBefore(start) && !createdDate.isAfter(end);
    }

    private AssessmentResponse toAdminResponse(ConstitutionAssessment assessment) {
        AssessmentResponse resp = new AssessmentResponse();
        resp.setId(assessment.getId());

        AssessmentResponse.UserInfo userInfo = new AssessmentResponse.UserInfo();
        User user = assessment.getUser();
        if (user != null) {
            userInfo.setName(Optional.ofNullable(user.getNickname())
                    .filter(StringUtils::hasText)
                    .orElse("用户" + user.getId()));

            Profile profile = profileRepository.findById(user.getId()).orElse(null);
            if (profile != null) {
                Sex sex = profile.getSex();
                if (sex == Sex.M) {
                    userInfo.setGender("男");
                } else if (sex == Sex.F) {
                    userInfo.setGender("女");
                } else {
                    userInfo.setGender("未知");
                }

                if (profile.getBirthday() != null) {
                    int age = Period.between(profile.getBirthday(), LocalDate.now()).getYears();
                    userInfo.setAge(Math.max(age, 0));
                }
            }
        }
        resp.setUser(userInfo);

        resp.setConstitutionType(mapPrimaryTypeToLabel(assessment.getPrimaryType()));

        if (assessment.getCreatedAt() != null) {
            Date assessmentDate = Date.from(
                    assessment.getCreatedAt()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );
            resp.setAssessmentDate(assessmentDate);
        }

        return resp;
    }

    private String mapPrimaryTypeToLabel(String primaryType) {
        if (!StringUtils.hasText(primaryType)) {
            return "平和质";
        }
        String code = primaryType.toUpperCase(Locale.ROOT);
        return switch (code) {
            case "BALANCED" -> "平和质";
            case "QI_DEFICIENCY" -> "气虚质";
            case "YANG_DEFICIENCY" -> "阳虚质";
            case "YIN_DEFICIENCY" -> "阴虚质";
            case "PHLEGM_DAMPNESS" -> "痰湿质";
            case "DAMP_HEAT" -> "湿热质";
            case "BLOOD_STASIS" -> "血瘀质";
            case "QI_STAGNATION" -> "气郁质";
            case "SPECIAL_DIATHESIS" -> "特禀质";
            default -> primaryType;
        };
    }
}
