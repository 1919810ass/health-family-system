package com.healthfamily.service;

import com.healthfamily.domain.entity.DoctorHealthReport;
import com.healthfamily.domain.repository.DoctorHealthReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorHealthReportService {

    private final DoctorHealthReportRepository repository;

    @Transactional
    public DoctorHealthReport createReport(Long doctorId, String doctorName, Long userId, Long familyId, String title, String diagnosis, String content, String pdfUrl) {
        DoctorHealthReport report = DoctorHealthReport.builder()
                .doctorId(doctorId)
                .doctorName(doctorName)
                .userId(userId)
                .familyId(familyId)
                .title(title)
                .diagnosis(diagnosis)
                .content(content)
                .pdfUrl(pdfUrl)
                .isRead(false)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        return repository.save(report);
    }

    public List<DoctorHealthReport> getReportsForPatient(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<DoctorHealthReport> getReportsForDoctor(Long doctorId) {
        return repository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
    }

    @Transactional
    public void markAsRead(Long reportId) {
        repository.findById(reportId).ifPresent(report -> {
            report.setIsRead(true);
            repository.save(report);
        });
    }

    public DoctorHealthReport getReport(Long id) {
        return repository.findById(id).orElse(null);
    }
}
