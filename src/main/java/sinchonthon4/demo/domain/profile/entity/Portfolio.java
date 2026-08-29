package sinchonthon4.demo.domain.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sinchonthon4.demo.domain.user.entity.User;

@Entity
@Getter
@Table(name = "portfolios")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portfolio {

    private static final String IMAGE_PORTFOLIO_TITLE = "portfolio-image";
    private static final LocalDate IMAGE_PORTFOLIO_STARTED_AT = LocalDate.of(1970, 1, 1);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 기존 DB의 NOT NULL 컬럼을 유지하기 위한 호환 필드
    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    // 기존 project_url 컬럼을 이미지 URL 저장소로 재사용한다.
    @Column(name = "project_url", length = 500)
    private String imageUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    // 기존 DB의 NOT NULL 컬럼을 유지하기 위한 호환 필드
    @Column(name = "started_at", nullable = false)
    private LocalDate startedAt;

    @Column(name = "ended_at")
    private LocalDate endedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Portfolio(User user, String imageUrl) {
        this.user = user;
        this.title = IMAGE_PORTFOLIO_TITLE;
        this.startedAt = IMAGE_PORTFOLIO_STARTED_AT;
        this.imageUrl = imageUrl;
    }

    public static Portfolio create(User user, String imageUrl) {
        return new Portfolio(user, imageUrl);
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
