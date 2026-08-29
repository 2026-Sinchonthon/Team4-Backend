package sinchonthon4.demo.domain.home.dto;

import java.util.List;
import sinchonthon4.demo.domain.home.repository.HomeRepository.HomeGroupSummaryRecord;
import sinchonthon4.demo.domain.home.repository.HomeRepository.HomeJobPostingRecord;
import sinchonthon4.demo.domain.home.repository.HomeRepository.HomeNetworkingEventRecord;
import sinchonthon4.demo.domain.home.repository.HomeRepository.HomeProfileRecord;
import sinchonthon4.demo.domain.home.repository.HomeRepository.HomeRecommendedGroupRecord;

public record HomeResponse(
        MyProfileResponse myProfile,
        List<ParticipatingGroupResponse> participatingGroups,
        List<RecommendedProfileResponse> recommendedProfiles,
        List<RecommendedGroupResponse> recommendedGroups,
        List<NetworkingEventResponse> networkingEvents,
        List<RecommendedJobPostingResponse> recommendedJobPostings
) {
    public HomeResponse {
        participatingGroups = List.copyOf(participatingGroups);
        recommendedProfiles = List.copyOf(recommendedProfiles);
        recommendedGroups = List.copyOf(recommendedGroups);
        networkingEvents = List.copyOf(networkingEvents);
        recommendedJobPostings = List.copyOf(recommendedJobPostings);
    }

    public record MyProfileResponse(
            Long userId, String name, String profileImageUrl, String school,
            String position, List<String> skills
    ) {
        public static MyProfileResponse from(HomeProfileRecord profile) {
            return new MyProfileResponse(
                    profile.userId(), profile.name(), profile.profileImageUrl(), profile.school(),
                    profile.position(), profile.skills()
            );
        }
    }

    public record ParticipatingGroupResponse(Long groupId, String title, String category) {
        public static ParticipatingGroupResponse from(HomeGroupSummaryRecord group) {
            return new ParticipatingGroupResponse(group.groupId(), group.title(), group.category());
        }
    }

    public record RecommendedProfileResponse(
            Long userId, String name, String profileImageUrl, String school,
            String position, List<String> skills
    ) {
        public static RecommendedProfileResponse from(HomeProfileRecord profile) {
            return new RecommendedProfileResponse(
                    profile.userId(), profile.name(), profile.profileImageUrl(), profile.school(),
                    profile.position(), profile.skills()
            );
        }
    }

    public record RecommendedGroupResponse(
            Long groupId, String title, String category, long currentMemberCount,
            int maxMemberCount, boolean isJoinAvailable
    ) {
        public static RecommendedGroupResponse from(HomeRecommendedGroupRecord group) {
            return new RecommendedGroupResponse(
                    group.groupId(), group.title(), group.category(), group.currentMemberCount(),
                    group.maxMemberCount(), group.joinAvailable()
            );
        }
    }

    public record NetworkingEventResponse(
            Long eventId, String eventType, String title, long currentParticipantCount,
            int maxParticipantCount, boolean isJoinAvailable
    ) {
        public static NetworkingEventResponse from(HomeNetworkingEventRecord event) {
            return new NetworkingEventResponse(
                    event.eventId(), event.eventType(), event.title(), event.currentParticipantCount(),
                    event.maxParticipantCount(), event.joinAvailable()
            );
        }
    }

    public record RecommendedJobPostingResponse(
            Long jobPostingId, String companyName, String title, String description,
            String location, String employmentType, String deadlineLabel, String thumbnailUrl
    ) {
        public static RecommendedJobPostingResponse from(HomeJobPostingRecord posting) {
            return new RecommendedJobPostingResponse(
                    posting.jobPostingId(), posting.companyName(), posting.title(), posting.description(),
                    posting.location(), posting.employmentType(), posting.deadlineLabel(), posting.thumbnailUrl()
            );
        }
    }
}
