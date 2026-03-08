package com.healthfamily.service;

import com.healthfamily.domain.constant.HealthLogType;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminHealthLogService {

    private final HealthLogRepository healthLogRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<HealthLog> searchLogs(
            String userKeyword,
            HealthLogType logType,
            LocalDate startDate,
            LocalDate endDate,
            String contentKeyword,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Data query
        CriteriaQuery<HealthLog> dataQuery = cb.createQuery(HealthLog.class);
        Root<HealthLog> dataRoot = dataQuery.from(HealthLog.class);
        dataRoot.fetch("user"); // Eager fetch to prevent N+1

        List<Predicate> predicates = new ArrayList<>();
        addPredicates(userKeyword, logType, startDate, endDate, contentKeyword, cb, dataRoot, predicates);
        dataQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        // Sorting
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
            for (Sort.Order order : sort) {
                if (order.isAscending()) {
                    orders.add(cb.asc(dataRoot.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(dataRoot.get(order.getProperty())));
                }
            }
            dataQuery.orderBy(orders);
        } else {
            dataQuery.orderBy(cb.desc(dataRoot.get("createdAt")));
        }

        List<HealthLog> logs = entityManager.createQuery(dataQuery)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<HealthLog> countRoot = countQuery.from(HealthLog.class);
        countQuery.select(cb.count(countRoot));

        List<Predicate> countPredicates = new ArrayList<>();
        addPredicates(userKeyword, logType, startDate, endDate, contentKeyword, cb, countRoot, countPredicates);
        countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(logs, pageable, total);
    }

    private void addPredicates(String userKeyword, HealthLogType logType, LocalDate startDate, LocalDate endDate, String contentKeyword, CriteriaBuilder cb, Root<HealthLog> root, List<Predicate> predicates) {
        if (StringUtils.hasText(userKeyword)) {
            Join<HealthLog, User> userJoin = root.join("user");
            predicates.add(cb.or(
                    cb.like(userJoin.get("nickname"), "%" + userKeyword + "%"),
                    cb.like(userJoin.get("phone"), "%" + userKeyword + "%")
            ));
        }
        if (logType != null) {
            predicates.add(cb.equal(root.get("type"), logType));
        }
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("logDate"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("logDate"), endDate));
        }
        if (StringUtils.hasText(contentKeyword)) {
            predicates.add(cb.like(root.get("contentJson"), "%" + contentKeyword + "%"));
        }
    }
}
