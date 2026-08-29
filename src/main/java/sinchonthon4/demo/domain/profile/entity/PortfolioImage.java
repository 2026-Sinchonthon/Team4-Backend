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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "portfolio_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    // 화살표로 넘길 때의 노출 순서 (0부터 시작)
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    PortfolioImage(Portfolio portfolio, String imageUrl, int sortOrder) {
        this.portfolio = portfolio;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
