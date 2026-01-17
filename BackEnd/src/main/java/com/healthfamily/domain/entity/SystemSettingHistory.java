package com.healthfamily.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "system_setting_histories")
public class SystemSettingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, length = 64)
    private String key;

    @Column(name = "config_value", nullable = false, columnDefinition = "text")
    private String value;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "created_by")
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Manual Builder implementation to resolve compilation issues
    public static SystemSettingHistoryBuilder builder() {
        return new SystemSettingHistoryBuilder();
    }

    public String getVersion() {
        return this.version;
    }
    
    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public Long getCreatedBy() {
        return this.createdBy;
    }

    public static class SystemSettingHistoryBuilder {
        private Long id;
        private String key;
        private String value;
        private String version;
        private Long createdBy;
        private LocalDateTime createdAt;

        SystemSettingHistoryBuilder() {
        }

        public SystemSettingHistoryBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SystemSettingHistoryBuilder key(String key) {
            this.key = key;
            return this;
        }

        public SystemSettingHistoryBuilder value(String value) {
            this.value = value;
            return this;
        }

        public SystemSettingHistoryBuilder version(String version) {
            this.version = version;
            return this;
        }

        public SystemSettingHistoryBuilder createdBy(Long createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public SystemSettingHistoryBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SystemSettingHistory build() {
            return new SystemSettingHistory(id, key, value, version, createdBy, createdAt);
        }

        public String toString() {
            return "SystemSettingHistory.SystemSettingHistoryBuilder(id=" + this.id + ", key=" + this.key + ", value=" + this.value + ", version=" + this.version + ", createdBy=" + this.createdBy + ", createdAt=" + this.createdAt + ")";
        }
    }
}
