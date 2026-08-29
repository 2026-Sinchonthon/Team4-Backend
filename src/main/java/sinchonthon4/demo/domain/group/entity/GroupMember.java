package sinchonthon4.demo.domain.group.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberRole;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberStatus;

/**
 * 모임 참가자. users N:M groups 의 중간 테이블 역할.
 * 동일 사용자의 중복 참가를 막기 위해 (group_id, user_id) 를 UNIQUE 로 둔다.
 * User 도메인은 다른 담당자가 관리하므로 userId(Long) FK 값으로만 참조한다.
 */
@Entity
@Getter
@Table(
        name = "group_members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_group_member_group_user",
                columnNames = {"group_id", "user_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupMemberStatus status;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private GroupMember(Group group, Long userId, GroupMemberRole role, GroupMemberStatus status) {
        this.group = group;
        this.userId = userId;
        this.role = role;
        this.status = status;
    }

    /** 모임장의 참가 승인. PENDING -> APPROVED */
    public void approve() {
        this.status = GroupMemberStatus.APPROVED;
    }

    /** 모임장의 참가 거절. PENDING -> REJECTED */
    public void reject() {
        this.status = GroupMemberStatus.REJECTED;
    }
}
