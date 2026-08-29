package sinchonthon4.demo.domain.profile.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private static final String DEFAULT_TITLE = "portfolio";
    private static final LocalDate DEFAULT_STARTED_AT = LocalDate.of(1970, 1, 1);

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

    // 기존 DB의 NOT NULL 컬럼을 유지하기 위한 호환 필드
    @Column(name = "started_at", nullable = false)
    private LocalDate startedAt;

    @Column(name = "ended_at")
    private LocalDate endedAt;

    @OneToMany(
            mappedBy = "portfolio",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("sortOrder ASC, id ASC")
    private List<PortfolioImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Portfolio(User user, String title, String description) {
        this.user = user;
        this.title = (title == null || title.isBlank()) ? DEFAULT_TITLE : title;
        this.description = description;
        this.startedAt = DEFAULT_STARTED_AT;
    }

    public static Portfolio create(User user, String title, String description, List<String> imageUrls) {
        Portfolio portfolio = new Portfolio(user, title, description);
        portfolio.replaceImages(imageUrls);
        return portfolio;
    }

    public void update(String title, String description, List<String> imageUrls) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        this.description = description;
        replaceImages(imageUrls);
    }

    public List<PortfolioImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    public List<String> getImageUrls() {
        return images.stream().map(PortfolioImage::getImageUrl).toList();
    }

    private void replaceImages(List<String> imageUrls) {
        images.clear();
        if (imageUrls == null) {
            return;
        }
        int order = 0;
        for (String imageUrl : imageUrls) {
            if (imageUrl == null || imageUrl.isBlank()) {
                continue;
            }
            images.add(new PortfolioImage(this, imageUrl.trim(), order++));
        }
    }
}
