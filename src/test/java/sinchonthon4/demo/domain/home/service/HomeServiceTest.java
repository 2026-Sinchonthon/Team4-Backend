package sinchonthon4.demo.domain.home.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sinchonthon4.demo.domain.home.exception.HomeProfileNotFoundException;
import sinchonthon4.demo.domain.home.repository.HomeRepository;
import sinchonthon4.demo.domain.home.repository.HomeRepository.HomeGroupSummaryRecord;
import sinchonthon4.demo.domain.home.repository.HomeRepository.HomeJobPostingRecord;
import sinchonthon4.demo.domain.home.repository.HomeRepository.HomeNetworkingEventRecord;
import sinchonthon4.demo.domain.home.repository.HomeRepository.HomeProfileRecord;
import sinchonthon4.demo.domain.home.repository.HomeRepository.HomeRecommendedGroupRecord;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    @Mock
    private HomeRepository homeRepository;

    @InjectMocks
    private HomeService homeService;

    @Test
    void returnsIntegratedHomeData() {
        HomeProfileRecord mine = new HomeProfileRecord(
                1L, "김지태", null, "서강대학교", "BACKEND", List.of("Java", "Spring Boot")
        );
        HomeProfileRecord recommended = new HomeProfileRecord(
                2L, "김신충", null, "홍익대학교", "UX_UI_DESIGNER", List.of("Figma", "Photoshop")
        );

        when(homeRepository.findProfile(1L)).thenReturn(Optional.of(mine));
        when(homeRepository.findParticipatingGroups(1L, 4))
                .thenReturn(List.of(new HomeGroupSummaryRecord(1L, "Spring boot 스터디", "STUDY")));
        when(homeRepository.findRecommendedProfiles(1L, 4)).thenReturn(List.of(recommended));
        when(homeRepository.findRecommendedGroups(1L, 4)).thenReturn(List.of(
                new HomeRecommendedGroupRecord(3L, "신촌 AI 사이드 프로젝트", "PROJECT", 3, 4, true)
        ));
        when(homeRepository.findNetworkingEvents(4)).thenReturn(List.of(
                new HomeNetworkingEventRecord(4L, "COFFEE_CHAT", "신촌 개발자 커피챗", 2, 4, true)
        ));
        when(homeRepository.findRecommendedJobPostings(4)).thenReturn(List.of(
                new HomeJobPostingRecord(
                        1L, "SincHub", "Backend Developer", "스타트업 포지션 채용",
                        "서울", "정식 채용", "D-5", null
                )
        ));

        var response = homeService.getHome(1L);

        assertThat(response.myProfile().userId()).isEqualTo(1L);
        assertThat(response.participatingGroups()).hasSize(1);
        assertThat(response.recommendedProfiles().getFirst().position()).isEqualTo("UX_UI_DESIGNER");
        assertThat(response.recommendedGroups().getFirst().isJoinAvailable()).isTrue();
        assertThat(response.networkingEvents()).hasSize(1);
        assertThat(response.recommendedJobPostings()).hasSize(1);
    }

    @Test
    void throwsWhenHomeProfileDoesNotExist() {
        when(homeRepository.findProfile(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeService.getHome(99L))
                .isInstanceOf(HomeProfileNotFoundException.class);
    }
}
