package sinchonthon4.demo.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import sinchonthon4.demo.domain.profile.entity.Portfolio;

@Schema(description = "포트폴리오")
public record PortfolioResponse(
        Long portfolioId,
        String title,
        String description,
        String projectUrl,
        String githubUrl,
        LocalDate startedAt,
        LocalDate endedAt
) {

    public static PortfolioResponse from(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getTitle(),
                portfolio.getDescription(),
                portfolio.getProjectUrl(),
                portfolio.getGithubUrl(),
                portfolio.getStartedAt(),
                portfolio.getEndedAt()
        );
    }
}
