package sinchonthon4.demo.domain.networking.repository;

import java.util.List;

public record NetworkingProfileRecord(
        Long profileId,
        Long userId,
        String name,
        String profileImageUrl,
        String school,
        String major,
        Integer grade,
        String position,
        String introduction,
        String githubUrl,
        String linkedinUrl,
        String portfolioUrl,
        List<String> skills
) {
    public NetworkingProfileRecord {
        skills = List.copyOf(skills);
    }

    public NetworkingProfileRecord withSkills(List<String> newSkills) {
        return new NetworkingProfileRecord(
                profileId, userId, name, profileImageUrl, school, major, grade,
                position, introduction, githubUrl, linkedinUrl, portfolioUrl, newSkills
        );
    }
}
