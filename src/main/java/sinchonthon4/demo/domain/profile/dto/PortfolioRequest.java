package sinchonthon4.demo.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "포트폴리오 이미지 생성 및 수정 요청")
public record PortfolioRequest(
        @Schema(example = "https://example.com/portfolio.png")
        @NotBlank(message = "포트폴리오 이미지 URL은 필수입니다.")
        @Size(max = 500, message = "포트폴리오 이미지 URL은 500자 이하여야 합니다.")
        String imageUrl
) {

    public PortfolioRequest {
        imageUrl = imageUrl == null ? null : imageUrl.trim();
    }
}
