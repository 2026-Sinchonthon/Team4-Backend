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
import sinchonthon4.demo.global.exception.BusinessException;
import sinchonthon4.demo.global.exception.ErrorCode;

@Entity
@Getter
@Table(name = "portfolios")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "project_url", length = 500)
    private String projectUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

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

    private Portfolio(User user, String title, String description, String projectUrl,
                      String githubUrl, LocalDate startedAt, LocalDate endedAt) {
        this.user = user;
        updateDetails(title, description, projectUrl, githubUrl, startedAt, endedAt);
    }

    public static Portfolio create(User user, String title, String description, String projectUrl,
                                   String githubUrl, LocalDate startedAt, LocalDate endedAt) {
        return new Portfolio(user, title, description, projectUrl, githubUrl, startedAt, endedAt);
    }

    public void updateDetails(String title, String description, String projectUrl,
                              String githubUrl, LocalDate startedAt, LocalDate endedAt) {
        validatePeriod(startedAt, endedAt);
        this.title = title;
        this.description = description;
        this.projectUrl = projectUrl;
        this.githubUrl = githubUrl;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    private void validatePeriod(LocalDate startedAt, LocalDate endedAt) {
        if (endedAt != null && endedAt.isBefore(startedAt)) {
            throw new BusinessException(ErrorCode.INVALID_PORTFOLIO_PERIOD);
        }
    }
}
