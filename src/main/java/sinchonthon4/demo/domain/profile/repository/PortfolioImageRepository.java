package sinchonthon4.demo.domain.profile.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sinchonthon4.demo.domain.profile.entity.PortfolioImage;

public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {

    List<PortfolioImage> findAllByPortfolio_IdOrderBySortOrderAscIdAsc(Long portfolioId);
}
