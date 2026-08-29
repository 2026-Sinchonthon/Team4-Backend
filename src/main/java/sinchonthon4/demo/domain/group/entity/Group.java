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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sinchonthon4.demo.domain.group.entity.enums.GroupStatus;

/**
 * 모임.
 * 생성 시 OWNER GroupMember 가 함께 생성되지만, 그 규칙은 Service Layer 에서 처리한다.
 */
@Entity
@Getter
// groups 는 MySQL 예약어이므로 백틱으로 이스케이프한다.
@Table(name = "`groups`")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 모임 생성자(모임장)의 userId.
     * User 도메인은 다른 담당자가 관리하므로 FK 값(Long)으로만 참조한다.
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private GroupCategory category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "meeting_at")
    private LocalDateTime meetingAt;

    @Column(name = "application_deadline")
    private LocalDateTime applicationDeadline;

    @Column(length = 255)
    private String location;

    @Column(name = "max_members", nullable = false)
    private int maxMembers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupStatus status;

    @Column(name = "open_chat_url", length = 500)
    private String openChatUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private Group(Long ownerId, GroupCategory category, String title, String description,
                  LocalDateTime meetingAt, LocalDateTime applicationDeadline, String location,
                  int maxMembers, GroupStatus status, String openChatUrl) {
        this.ownerId = ownerId;
        this.category = category;
        this.title = title;
        this.description = description;
        this.meetingAt = meetingAt;
        this.applicationDeadline = applicationDeadline;
        this.location = location;
        this.maxMembers = maxMembers;
        this.status = status != null ? status : GroupStatus.RECRUITING;
        this.openChatUrl = openChatUrl;
    }

    public void update(GroupCategory category, String title, String description, String location,
                       LocalDateTime meetingAt, LocalDateTime applicationDeadline,
                       int maxMembers, String openChatUrl) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.location = location;
        this.meetingAt = meetingAt;
        this.applicationDeadline = applicationDeadline;
        this.maxMembers = maxMembers;
        this.openChatUrl = openChatUrl;
    }

    public void changeStatus(GroupStatus status) {
        this.status = status;
    }
}
