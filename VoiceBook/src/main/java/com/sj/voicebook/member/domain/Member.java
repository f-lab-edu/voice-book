package com.sj.voicebook.member.domain;

import com.sj.voicebook.global.Role;
import com.sj.voicebook.global.MemberStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "members",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_email", columnNames = "email"),
                @UniqueConstraint(name = "unique_nickname", columnNames = "nickname")
        }
)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String email;

    @Column(length = 100)
    private String password;

    @Column(nullable = false, length = 20)
    private String nickname;

    private String profileImage;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private SocialType socialType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.ROLE_USER; // ROLE_USER, ROLE_ADMIN

    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    private Member(String email, String encryptedPassword, String nickname, String profileImage) {
        this.email = email;
        this.password = encryptedPassword;
        this.nickname = nickname;
        this.profileImage = profileImage;

    }

    public static Member create(String email, String encryptedPassword, String nickname, String profileImage) {
        return new Member(email, encryptedPassword, nickname, profileImage);
    }

    public void updateLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

}
