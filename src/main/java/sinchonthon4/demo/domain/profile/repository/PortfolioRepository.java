package sinchonthon4.demo.domain.profile.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sinchonthon4.demo.domain.profile.entity.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findAllByUser_IdOrderByCreatedAtDescIdDesc(Long userId);
}
