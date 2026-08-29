package sinchonthon4.demo.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "포트폴리오 생성 및 전체 수정 요청")
public record PortfolioRequest(
        @Schema(example = "신촌톤 프로젝트")
        @NotBlank(message = "포트폴리오 제목은 필수입니다.")
        @Size(max = 100, message = "포트폴리오 제목은 100자 이하여야 합니다.")
        String title,

        @Size(max = 500, message = "포트폴리오 설명은 500자 이하여야 합니다.")
        String description,

        @Schema(example = "https://example.com")
        @Size(max = 500, message = "프로젝트 URL은 500자 이하여야 합니다.")
        String projectUrl,

        @Schema(example = "https://github.com/example/project")
        @Size(max = 500, message = "GitHub URL은 500자 이하여야 합니다.")
        String githubUrl,

        @Schema(example = "2026-08-29")
        @NotNull(message = "프로젝트 시작일은 필수입니다.")
        LocalDate startedAt,

        @Schema(example = "2026-08-31")
        LocalDate endedAt
) {

    public PortfolioRequest {
        title = trim(title);
        description = trimToNull(description);
        projectUrl = trimToNull(projectUrl);
        githubUrl = trimToNull(githubUrl);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
