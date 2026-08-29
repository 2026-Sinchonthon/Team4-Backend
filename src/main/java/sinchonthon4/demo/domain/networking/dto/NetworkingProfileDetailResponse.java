package sinchonthon4.demo.domain.networking.dto;

import java.util.List;
import sinchonthon4.demo.domain.networking.repository.NetworkingProfileRecord;

public record NetworkingProfileDetailResponse(
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
    public NetworkingProfileDetailResponse {
        skills = List.copyOf(skills);
    }

    public static NetworkingProfileDetailResponse from(NetworkingProfileRecord profile) {
        return new NetworkingProfileDetailResponse(
                profile.userId(), profile.name(), profile.profileImageUrl(), profile.school(),
                profile.major(), profile.grade(), profile.position(), profile.introduction(),
                profile.githubUrl(), profile.linkedinUrl(), profile.portfolioUrl(), profile.skills()
        );
    }
}
