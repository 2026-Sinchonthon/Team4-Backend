package sinchonthon4.demo.domain.networking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import sinchonthon4.demo.domain.networking.entity.enums.NetworkingEventStatus;
import sinchonthon4.demo.domain.networking.entity.enums.NetworkingEventType;

@Entity
@Getter
@Table(name = "networking_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NetworkingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private NetworkingEventType eventType;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String location;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NetworkingEventStatus status;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private NetworkingEvent(NetworkingEventType eventType, String title, String description,
                            String location, int maxParticipants, NetworkingEventStatus status,
                            String thumbnailUrl) {
        this.eventType = eventType;
        this.title = title;
        this.description = description;
        this.location = location;
        this.maxParticipants = maxParticipants;
        this.status = status;
        this.thumbnailUrl = thumbnailUrl;
    }
}
