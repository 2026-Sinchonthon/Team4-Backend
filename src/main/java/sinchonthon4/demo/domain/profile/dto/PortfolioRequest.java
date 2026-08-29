package sinchonthon4.demo.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "포트폴리오 생성 및 수정 요청")
public record PortfolioRequest(
        @Schema(example = "내 사이드 프로젝트 모음")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @Schema(example = "포트폴리오 설명")
        @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
        String description,

        @Schema(example = "[\"https://example.com/1.png\", \"https://example.com/2.png\"]")
        @NotEmpty(message = "포트폴리오 이미지는 최소 1장 이상이어야 합니다.")
        @Size(max = 30, message = "포트폴리오 이미지는 최대 30장까지 등록할 수 있습니다.")
        List<@Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다.") String> imageUrls
) {

    public PortfolioRequest {
        title = title == null ? null : title.trim();
        description = description == null ? null : description.trim();
        imageUrls = imageUrls == null
                ? List.of()
                : imageUrls.stream()
                        .filter(url -> url != null && !url.isBlank())
                        .map(String::trim)
                        .toList();
    }
}
