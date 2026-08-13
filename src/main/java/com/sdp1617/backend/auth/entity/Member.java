package com.sdp1617.backend.auth.entity;

import com.sdp1617.backend.auth.util.FollowCodeGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_provider_provider_id",
                columnNames = {"provider", "provider_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    private static final int MAX_FAILED_LOGIN_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(nullable = false)
    private boolean termsAgreed;

    @Column(nullable = false)
    private int failedLoginCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "follow_code", nullable = false, unique = true, length = 12)
    private String followCode;
    @Column(name = "push_notification_enabled", nullable = false)
    private boolean pushNotificationEnabled;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Member(String email, String password, String nickname, boolean termsAgreed) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.termsAgreed = termsAgreed;
        this.failedLoginCount = 0;
        this.provider = AuthProvider.LOCAL;
        this.pushNotificationEnabled = true;
        this.createdAt = LocalDateTime.now();
    }

    public Member(String email, String nickname, boolean termsAgreed, AuthProvider provider, String providerId) {
        if (provider == AuthProvider.LOCAL || providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("소셜 회원은 LOCAL이 아닌 provider와 providerId가 필요합니다.");
        }
        this.email = email;
        this.password = null;
        this.nickname = nickname;
        this.termsAgreed = termsAgreed;
        this.failedLoginCount = 0;
        this.provider = provider;
        this.providerId = providerId;
        this.pushNotificationEnabled = true;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isLocked() {
        return failedLoginCount >= MAX_FAILED_LOGIN_COUNT;
    }

    public boolean hasPassword() {
        return password != null;
    }

    public void increaseFailedLoginCount() {
        this.failedLoginCount++;
    }

    public void resetFailedLoginCount() {
        this.failedLoginCount = 0;
    }

    public void unlock() {
        resetFailedLoginCount();
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void reissueFollowCode() {
        this.followCode = FollowCodeGenerator.generate();
    }

    @PrePersist
    private void assignFollowCodeIfMissing() {
        if (this.followCode == null) {
            this.followCode = FollowCodeGenerator.generate();
        }
    public void updatePushNotificationEnabled(boolean pushNotificationEnabled) {
        this.pushNotificationEnabled = pushNotificationEnabled;
    }
}
