package sinchonthon4.demo.domain.home.repository;

import java.util.List;
import java.util.Optional;

public interface HomeRepository {

    Optional<HomeProfileRecord> findProfile(Long userId);

    List<HomeGroupSummaryRecord> findParticipatingGroups(Long userId, int limit);

    List<HomeProfileRecord> findRecommendedProfiles(Long userId, int limit);

    List<HomeRecommendedGroupRecord> findRecommendedGroups(Long userId, int limit);

    List<HomeNetworkingEventRecord> findNetworkingEvents(int limit);

    List<HomeJobPostingRecord> findRecommendedJobPostings(int limit);

    record HomeProfileRecord(
            Long userId,
            String name,
            String profileImageUrl,
            String school,
            String position,
            List<String> skills
    ) {
        public HomeProfileRecord {
            skills = List.copyOf(skills);
        }

        public HomeProfileRecord withSkills(List<String> newSkills) {
            return new HomeProfileRecord(userId, name, profileImageUrl, school, position, newSkills);
        }
    }

    record HomeGroupSummaryRecord(Long groupId, String title, String category) {
    }

    record HomeRecommendedGroupRecord(
            Long groupId,
            String title,
            String category,
            long currentMemberCount,
            int maxMemberCount,
            boolean joinAvailable
    ) {
    }

    record HomeNetworkingEventRecord(
            Long eventId,
            String eventType,
            String title,
            long currentParticipantCount,
            int maxParticipantCount,
            boolean joinAvailable
    ) {
    }

    record HomeJobPostingRecord(
            Long jobPostingId,
            String companyName,
            String title,
            String description,
            String location,
            String employmentType,
            String deadlineLabel,
            String thumbnailUrl
    ) {
    }
}
