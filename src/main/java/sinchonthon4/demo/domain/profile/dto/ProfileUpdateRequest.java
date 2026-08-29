package sinchonthon4.demo.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import sinchonthon4.demo.domain.profile.entity.Position;

@Schema(description = "내 프로필과 기술 스택 전체 수정 요청")
public record ProfileUpdateRequest(
        @Schema(example = "길동")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname,

        @Schema(example = "연세대학교")
        @NotBlank(message = "학교는 필수입니다.")
        @Size(max = 100, message = "학교는 100자 이하여야 합니다.")
        String school,

        @Schema(example = "컴퓨터과학과")
        @NotBlank(message = "전공은 필수입니다.")
        @Size(max = 100, message = "전공은 100자 이하여야 합니다.")
        String major,

        @Schema(example = "4")
        @NotNull(message = "학년은 필수입니다.")
        @Positive(message = "학년은 양수여야 합니다.")
        Integer grade,

        @Schema(example = "BACKEND")
        @NotNull(message = "관심 직무는 필수입니다.")
        Position position,

        @Size(max = 500, message = "자기소개는 500자 이하여야 합니다.")
        String introduction,

        @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다.")
        String profileImageUrl,

        @Schema(example = "https://github.com/example")
        @Size(max = 500, message = "GitHub URL은 500자 이하여야 합니다.")
        String githubUrl,

        @Size(max = 500, message = "LinkedIn URL은 500자 이하여야 합니다.")
        String linkedinUrl,

        @Size(max = 500, message = "포트폴리오 URL은 500자 이하여야 합니다.")
        String portfolioUrl,

        @Schema(example = "[1, 2, 5]")
        @NotNull(message = "기술 스택 목록은 필수입니다.")
        List<@NotNull(message = "기술 스택 ID는 null일 수 없습니다.")
                @Positive(message = "기술 스택 ID는 양수여야 합니다.") Long> skillIds
) {

    public ProfileUpdateRequest {
        nickname = trim(nickname);
        school = trim(school);
        major = trim(major);
        introduction = trimToNull(introduction);
        profileImageUrl = trimToNull(profileImageUrl);
        githubUrl = trimToNull(githubUrl);
        linkedinUrl = trimToNull(linkedinUrl);
        portfolioUrl = trimToNull(portfolioUrl);
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
