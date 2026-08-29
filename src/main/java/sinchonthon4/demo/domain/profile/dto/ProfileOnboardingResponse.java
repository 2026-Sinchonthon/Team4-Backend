package sinchonthon4.demo.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import sinchonthon4.demo.domain.profile.entity.Position;
import sinchonthon4.demo.domain.profile.entity.Profile;
import sinchonthon4.demo.domain.profile.entity.Skill;

@Schema(description = "등록된 온보딩 프로필")
public record ProfileOnboardingResponse(
        Long profileId,
        Long userId,
        String nickname,
        String school,
        String major,
        int grade,
        Position position,
        String introduction,
        String profileImageUrl,
        String githubUrl,
        String linkedinUrl,
        String portfolioUrl,
        List<SkillResponse> skills
) {

    public static ProfileOnboardingResponse of(Profile profile, List<Skill> skills) {
        return new ProfileOnboardingResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getNickname(),
                profile.getSchool(),
                profile.getMajor(),
                profile.getGrade(),
                profile.getPosition(),
                profile.getIntroduction(),
                profile.getProfileImageUrl(),
                profile.getGithubUrl(),
                profile.getLinkedinUrl(),
                profile.getPortfolioUrl(),
                skills.stream().map(SkillResponse::from).toList()
        );
    }
}
