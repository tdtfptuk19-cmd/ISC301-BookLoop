package com.bookloop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Key-value platform configuration setting (e.g. promotion fee, duration, payment instructions).
 */
@Entity
@Table(name = "platform_settings")
public class PlatformSetting {

    @Id
    @NotBlank
    @Size(max = 100)
    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String settingKey;

    @NotBlank
    @Size(max = 2000)
    @Column(name = "setting_value", nullable = false, columnDefinition = "NVARCHAR(2000)")
    private String settingValue;

    @Size(max = 500)
    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PlatformSetting() {}

    public PlatformSetting(String settingKey, String settingValue, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

