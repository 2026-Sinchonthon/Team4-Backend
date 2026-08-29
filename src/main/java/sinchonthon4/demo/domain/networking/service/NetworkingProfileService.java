package sinchonthon4.demo.domain.networking.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.networking.dto.NetworkingProfileDetailResponse;
import sinchonthon4.demo.domain.networking.dto.NetworkingProfilePageResponse;
import sinchonthon4.demo.domain.networking.dto.NetworkingProfileSearchCondition;
import sinchonthon4.demo.domain.networking.dto.NetworkingProfileSummaryResponse;
import sinchonthon4.demo.domain.networking.exception.InvalidNetworkingProfileSearchException;
import sinchonthon4.demo.domain.networking.exception.NetworkingProfileNotFoundException;
import sinchonthon4.demo.domain.networking.repository.NetworkingProfilePage;
import sinchonthon4.demo.domain.networking.repository.NetworkingProfileRecord;
import sinchonthon4.demo.domain.networking.repository.NetworkingProfileRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NetworkingProfileService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> POSITIONS = Set.of(
            "BACKEND", "FRONTEND", "FULLSTACK", "AI_ML", "DATA", "MOBILE",
            "DESIGN", "PM", "MARKETING", "BUSINESS", "OTHER"
    );

    private final NetworkingProfileRepository networkingProfileRepository;

    public NetworkingProfilePageResponse getProfiles(NetworkingProfileSearchCondition condition) {
        validate(condition);
        NetworkingProfilePage result = networkingProfileRepository.findAll(condition);
        int totalPages = result.totalElements() == 0
                ? 0
                : (int) Math.ceil((double) result.totalElements() / condition.size());

        return new NetworkingProfilePageResponse(
                result.content().stream().map(NetworkingProfileSummaryResponse::from).toList(),
                condition.page(), condition.size(), result.totalElements(), totalPages
        );
    }

    public NetworkingProfileDetailResponse getProfile(Long userId) {
        if (userId == null || userId <= 0) {
            throw new InvalidNetworkingProfileSearchException("userId는 1 이상이어야 합니다.");
        }

        NetworkingProfileRecord profile = networkingProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NetworkingProfileNotFoundException(userId));
        return NetworkingProfileDetailResponse.from(profile);
    }

    private void validate(NetworkingProfileSearchCondition condition) {
        if (condition.page() < 0) {
            throw new InvalidNetworkingProfileSearchException("page는 0 이상이어야 합니다.");
        }
        if (condition.size() < 1 || condition.size() > MAX_PAGE_SIZE) {
            throw new InvalidNetworkingProfileSearchException("size는 1 이상 100 이하여야 합니다.");
        }
        if (condition.skillId() != null && condition.skillId() <= 0) {
            throw new InvalidNetworkingProfileSearchException("skillId는 1 이상이어야 합니다.");
        }
        if (condition.position() != null
                && !condition.position().isBlank()
                && !POSITIONS.contains(condition.position().trim())) {
            throw new InvalidNetworkingProfileSearchException("지원하지 않는 position 값입니다.");
        }
    }
}
