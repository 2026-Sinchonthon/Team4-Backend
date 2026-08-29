package sinchonthon4.demo.domain.home.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.home.dto.HomeResponse;
import sinchonthon4.demo.domain.home.dto.HomeResponse.MyProfileResponse;
import sinchonthon4.demo.domain.home.dto.HomeResponse.NetworkingEventResponse;
import sinchonthon4.demo.domain.home.dto.HomeResponse.ParticipatingGroupResponse;
import sinchonthon4.demo.domain.home.dto.HomeResponse.RecommendedGroupResponse;
import sinchonthon4.demo.domain.home.dto.HomeResponse.RecommendedJobPostingResponse;
import sinchonthon4.demo.domain.home.dto.HomeResponse.RecommendedProfileResponse;
import sinchonthon4.demo.domain.home.exception.HomeProfileNotFoundException;
import sinchonthon4.demo.domain.home.repository.HomeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private static final int SECTION_LIMIT = 4;

    private final HomeRepository homeRepository;

    public HomeResponse getHome(Long userId) {
        if (userId == null || userId <= 0) {
            throw new HomeProfileNotFoundException(userId);
        }

        var myProfile = homeRepository.findProfile(userId)
                .orElseThrow(() -> new HomeProfileNotFoundException(userId));

        return new HomeResponse(
                MyProfileResponse.from(myProfile),
                homeRepository.findParticipatingGroups(userId, SECTION_LIMIT).stream()
                        .map(ParticipatingGroupResponse::from).toList(),
                homeRepository.findRecommendedProfiles(userId, SECTION_LIMIT).stream()
                        .map(RecommendedProfileResponse::from).toList(),
                homeRepository.findRecommendedGroups(userId, SECTION_LIMIT).stream()
                        .map(RecommendedGroupResponse::from).toList(),
                homeRepository.findNetworkingEvents(SECTION_LIMIT).stream()
                        .map(NetworkingEventResponse::from).toList(),
                homeRepository.findRecommendedJobPostings(SECTION_LIMIT).stream()
                        .map(RecommendedJobPostingResponse::from).toList()
        );
    }
}
