package sinchonthon4.demo.domain.networking.service;

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
import sinchonthon4.demo.domain.networking.dto.NetworkingProfilePageResponse;
import sinchonthon4.demo.domain.networking.dto.NetworkingProfileSearchCondition;
import sinchonthon4.demo.domain.networking.exception.InvalidNetworkingProfileSearchException;
import sinchonthon4.demo.domain.networking.exception.NetworkingProfileNotFoundException;
import sinchonthon4.demo.domain.networking.repository.NetworkingProfilePage;
import sinchonthon4.demo.domain.networking.repository.NetworkingProfileRecord;
import sinchonthon4.demo.domain.networking.repository.NetworkingProfileRepository;

@ExtendWith(MockitoExtension.class)
class NetworkingProfileServiceTest {

    @Mock
    private NetworkingProfileRepository networkingProfileRepository;

    @InjectMocks
    private NetworkingProfileService networkingProfileService;

    @Test
    void searchesProfilesWithPagination() {
        NetworkingProfileSearchCondition condition = new NetworkingProfileSearchCondition(
                "김", "홍익대학교", null, "DESIGN", 10L, 0, 20
        );
        when(networkingProfileRepository.findAll(condition))
                .thenReturn(new NetworkingProfilePage(List.of(profile()), 21));

        NetworkingProfilePageResponse response = networkingProfileService.getProfiles(condition);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().skills()).containsExactly("Figma", "Photoshop");
        assertThat(response.totalElements()).isEqualTo(21);
        assertThat(response.totalPages()).isEqualTo(2);
    }

    @Test
    void rejectsInvalidPageSize() {
        NetworkingProfileSearchCondition condition = new NetworkingProfileSearchCondition(
                null, null, null, null, null, 0, 101
        );

        assertThatThrownBy(() -> networkingProfileService.getProfiles(condition))
                .isInstanceOf(InvalidNetworkingProfileSearchException.class);
    }

    @Test
    void acceptsApiPositionAlias() {
        NetworkingProfileSearchCondition condition = new NetworkingProfileSearchCondition(
                null, null, null, "UX_UI_DESIGNER", null, 0, 20
        );
        when(networkingProfileRepository.findAll(condition))
                .thenReturn(new NetworkingProfilePage(List.of(profile()), 1));

        assertThat(networkingProfileService.getProfiles(condition).content()).hasSize(1);
    }

    @Test
    void returnsProfileDetail() {
        when(networkingProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile()));

        var response = networkingProfileService.getProfile(2L);

        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.position()).isEqualTo("UX_UI_DESIGNER");
        assertThat(response.skills()).containsExactly("Figma", "Photoshop");
    }

    @Test
    void throwsWhenProfileDoesNotExist() {
        when(networkingProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> networkingProfileService.getProfile(99L))
                .isInstanceOf(NetworkingProfileNotFoundException.class);
    }

    private NetworkingProfileRecord profile() {
        return new NetworkingProfileRecord(
                1L, 2L, "김신충", null, "홍익대학교", "시각디자인과", 3,
                "UX_UI_DESIGNER", "사용자 경험을 설계하는 디자이너입니다.",
                "https://github.com/example", null, null, List.of("Figma", "Photoshop")
        );
    }
}
