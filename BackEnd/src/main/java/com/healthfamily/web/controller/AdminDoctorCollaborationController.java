package com.healthfamily.web.controller;

import com.healthfamily.service.AdminDoctorCollaborationService;
import com.healthfamily.web.dto.DoctorCollaborationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.healthfamily.web.dto.DoctorCollaborationDetailDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/collaboration")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDoctorCollaborationController {

    private final AdminDoctorCollaborationService collaborationService;

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorCollaborationDto>> getDoctorCollaborationStats() {
        return ResponseEntity.ok(collaborationService.getDoctorCollaborationStats());
    }

    @GetMapping("/doctors/{id}/detail")
    public ResponseEntity<DoctorCollaborationDetailDto> getDoctorDetail(@PathVariable Long id) {
        return ResponseEntity.ok(collaborationService.getDoctorDetail(id));
    }

    @PostMapping("/doctors/assign")
    public ResponseEntity<Void> assignTask(@RequestBody Map<String, Long> payload) {
        Long sourceDoctorId = payload.get("sourceDoctorId");
        Long targetDoctorId = payload.get("targetDoctorId");
        collaborationService.assignTask(sourceDoctorId, targetDoctorId);
        return ResponseEntity.ok().build();
    }
}
