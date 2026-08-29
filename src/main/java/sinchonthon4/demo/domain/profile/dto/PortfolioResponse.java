package sinchonthon4.demo.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import sinchonthon4.demo.domain.profile.entity.Portfolio;

@Schema(description = "포트폴리오 이미지")
public record PortfolioResponse(
        Long portfolioId,
        String imageUrl
) {

    public static PortfolioResponse from(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getImageUrl()
        );
    }
}
