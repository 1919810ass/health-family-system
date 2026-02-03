package com.healthfamily.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.constant.UserRole;
import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.FamilyMember;
import com.healthfamily.domain.entity.HealthReminder;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.FamilyMemberRepository;
import com.healthfamily.domain.repository.FamilyRepository;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.HealthReminderRepository;
import com.healthfamily.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HealthReminderServiceImplTest {

    private HealthReminderServiceImpl newService(
            ChatModel chatModel,
            ObjectMapper objectMapper,
            HealthReminderRepository reminderRepository,
            UserRepository userRepository,
            HealthLogRepository healthLogRepository,
            FamilyRepository familyRepository,
            FamilyMemberRepository familyMemberRepository
    ) {
        return new HealthReminderServiceImpl(
                chatModel,
                objectMapper,
                reminderRepository,
                userRepository,
                healthLogRepository,
                familyRepository,
                familyMemberRepository
        );
    }

    @Test
    void deleteReminder_allowsFamilyMemberAdmin() {
        ChatModel chatModel = Mockito.mock(ChatModel.class);
        ObjectMapper objectMapper = new ObjectMapper();
        HealthReminderRepository reminderRepository = Mockito.mock(HealthReminderRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        HealthLogRepository healthLogRepository = Mockito.mock(HealthLogRepository.class);
        FamilyRepository familyRepository = Mockito.mock(FamilyRepository.class);
        FamilyMemberRepository familyMemberRepository = Mockito.mock(FamilyMemberRepository.class);

        HealthReminderServiceImpl service = newService(
                chatModel, objectMapper, reminderRepository, userRepository, healthLogRepository, familyRepository, familyMemberRepository
        );

        User admin = User.builder().id(10L).role(UserRole.MEMBER).build();
        Family family = Family.builder().id(1L).owner(User.builder().id(99L).build()).build();
        HealthReminder reminder = HealthReminder.builder()
                .id(79L)
                .user(User.builder().id(20L).build())
                .assignedTo(User.builder().id(30L).build())
                .creator(User.builder().id(40L).build())
                .family(family)
                .build();

        FamilyMember fm = FamilyMember.builder().id(1L).family(family).user(admin).admin(true).build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(admin));
        when(reminderRepository.findById(79L)).thenReturn(Optional.of(reminder));
        when(familyMemberRepository.findByFamilyAndUser(family, admin)).thenReturn(Optional.of(fm));

        assertDoesNotThrow(() -> service.deleteReminder(10L, 79L));
        verify(reminderRepository).delete(reminder);
    }

    @Test
    void deleteReminder_allowsFamilyAdminRoleWhenInFamily() {
        ChatModel chatModel = Mockito.mock(ChatModel.class);
        ObjectMapper objectMapper = new ObjectMapper();
        HealthReminderRepository reminderRepository = Mockito.mock(HealthReminderRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        HealthLogRepository healthLogRepository = Mockito.mock(HealthLogRepository.class);
        FamilyRepository familyRepository = Mockito.mock(FamilyRepository.class);
        FamilyMemberRepository familyMemberRepository = Mockito.mock(FamilyMemberRepository.class);

        HealthReminderServiceImpl service = newService(
                chatModel, objectMapper, reminderRepository, userRepository, healthLogRepository, familyRepository, familyMemberRepository
        );

        User familyAdmin = User.builder().id(10L).role(UserRole.FAMILY_ADMIN).build();
        Family family = Family.builder().id(1L).owner(User.builder().id(99L).build()).build();
        HealthReminder reminder = HealthReminder.builder()
                .id(79L)
                .user(User.builder().id(20L).build())
                .family(family)
                .build();

        FamilyMember fm = FamilyMember.builder().id(1L).family(family).user(familyAdmin).admin(false).build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(familyAdmin));
        when(reminderRepository.findById(79L)).thenReturn(Optional.of(reminder));
        when(familyMemberRepository.findByFamilyAndUser(family, familyAdmin)).thenReturn(Optional.of(fm));

        assertDoesNotThrow(() -> service.deleteReminder(10L, 79L));
        verify(reminderRepository).delete(reminder);
    }

    @Test
    void deleteReminder_deniesFamilyAdminRoleWhenNotInFamily() {
        ChatModel chatModel = Mockito.mock(ChatModel.class);
        ObjectMapper objectMapper = new ObjectMapper();
        HealthReminderRepository reminderRepository = Mockito.mock(HealthReminderRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        HealthLogRepository healthLogRepository = Mockito.mock(HealthLogRepository.class);
        FamilyRepository familyRepository = Mockito.mock(FamilyRepository.class);
        FamilyMemberRepository familyMemberRepository = Mockito.mock(FamilyMemberRepository.class);

        HealthReminderServiceImpl service = newService(
                chatModel, objectMapper, reminderRepository, userRepository, healthLogRepository, familyRepository, familyMemberRepository
        );

        User familyAdmin = User.builder().id(10L).role(UserRole.FAMILY_ADMIN).build();
        Family family = Family.builder().id(1L).owner(User.builder().id(99L).build()).build();
        HealthReminder reminder = HealthReminder.builder()
                .id(79L)
                .user(User.builder().id(20L).build())
                .family(family)
                .build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(familyAdmin));
        when(reminderRepository.findById(79L)).thenReturn(Optional.of(reminder));
        when(familyMemberRepository.findByFamilyAndUser(any(Family.class), any(User.class))).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.deleteReminder(10L, 79L));
    }
}

