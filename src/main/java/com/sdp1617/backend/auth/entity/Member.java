package com.sdp1617.backend.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    private static final int MAX_FAILED_LOGIN_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(nullable = false)
    private boolean termsAgreed;

    @Column(nullable = false)
    private int failedLoginCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Member(String email, String password, String nickname, boolean termsAgreed) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.termsAgreed = termsAgreed;
        this.failedLoginCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isLocked() {
        return failedLoginCount >= MAX_FAILED_LOGIN_COUNT;
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
}
