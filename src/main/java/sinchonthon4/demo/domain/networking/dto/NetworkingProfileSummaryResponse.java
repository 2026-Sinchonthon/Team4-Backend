package sinchonthon4.demo.domain.networking.dto;

import java.util.List;
import sinchonthon4.demo.domain.networking.repository.NetworkingProfileRecord;

public record NetworkingProfileSummaryResponse(
        Long userId,
        String name,
        String profileImageUrl,
        String school,
        String major,
        String position,
        String introduction,
        List<String> skills
) {
    public NetworkingProfileSummaryResponse {
        skills = List.copyOf(skills);
    }

    public static NetworkingProfileSummaryResponse from(NetworkingProfileRecord profile) {
        return new NetworkingProfileSummaryResponse(
                profile.userId(), profile.name(), profile.profileImageUrl(), profile.school(),
                profile.major(), profile.position(), profile.introduction(), profile.skills()
        );
    }
}
