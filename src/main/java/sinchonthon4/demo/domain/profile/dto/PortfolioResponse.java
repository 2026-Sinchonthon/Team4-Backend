package sinchonthon4.demo.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import sinchonthon4.demo.domain.profile.entity.Portfolio;

@Schema(description = "포트폴리오")
public record PortfolioResponse(
        Long portfolioId,
        String title,
        String description,
        @Schema(description = "노출 순서대로 정렬된 이미지 URL 목록")
        List<String> imageUrls
) {

    public static PortfolioResponse from(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getTitle(),
                portfolio.getDescription(),
                portfolio.getImageUrls()
        );
    }
}
