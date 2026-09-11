package com.project.souklab.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "uk_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_status", columnList = "status, deleted_at"),
        @Index(name = "idx_users_phone", columnList = "phone")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 30)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AccountStatus status = AccountStatus.PENDING;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "banned_until")
    private LocalDateTime bannedUntil;

    @Column(name = "ban_reason", columnDefinition = "TEXT")
    private String banReason;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<OAuthIdentity> oauthIdentities = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Artisan artisan;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Client client;

    public String getName() {
        if (firstName != null && lastName != null) {
            return firstName.trim() + " " + lastName.trim();
        } else if (firstName != null) {
            return firstName.trim();
        } else if (lastName != null) {
            return lastName.trim();
        }
        return email;
    }

    /**
     * Determines whether this user has an active suspension as of the specified point in time.
     * A suspension is considered active if the status is {@link AccountStatus#SUSPENDED}
     * and the ban is either permanent (bannedUntil is null) or the timeout expiration timestamp
     * is strictly in the future.
     *
     * @param now the reference point in time
     * @return true if the user is actively suspended, false otherwise
     */
    public boolean isSuspensionActive(LocalDateTime now) {
        return status == AccountStatus.SUSPENDED && (bannedUntil == null || bannedUntil.isAfter(now));
    }

    /**
     * Computes the effective account status as of the specified point in time.
     * If the persisted status is {@link AccountStatus#SUSPENDED} but the timeout has expired,
     * this returns {@link AccountStatus#ACTIVE}. Otherwise, returns the persisted status.
     *
     * @param now the reference point in time
     * @return the effective {@link AccountStatus}
     */
    public AccountStatus getEffectiveStatus(LocalDateTime now) {
        if (status == AccountStatus.SUSPENDED && !isSuspensionActive(now)) {
            return AccountStatus.ACTIVE;
        }
        return status;
    }
}
