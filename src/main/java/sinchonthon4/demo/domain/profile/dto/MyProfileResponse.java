package sinchonthon4.demo.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import sinchonthon4.demo.domain.profile.entity.Position;
import sinchonthon4.demo.domain.profile.entity.Profile;
import sinchonthon4.demo.domain.profile.entity.Skill;
import sinchonthon4.demo.domain.user.entity.User;

@Schema(description = "마이페이지 프로필")
public record MyProfileResponse(
        Long userId,
        String email,
        String name,
        Long profileId,
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

    public static MyProfileResponse of(User user, Profile profile, List<Skill> skills) {
        return new MyProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                profile.getId(),
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
