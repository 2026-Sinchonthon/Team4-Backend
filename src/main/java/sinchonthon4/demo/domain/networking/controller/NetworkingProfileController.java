package sinchonthon4.demo.domain.networking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sinchonthon4.demo.domain.networking.dto.NetworkingProfileDetailResponse;
import sinchonthon4.demo.domain.networking.dto.NetworkingProfilePageResponse;
import sinchonthon4.demo.domain.networking.dto.NetworkingProfileSearchCondition;
import sinchonthon4.demo.domain.networking.service.NetworkingProfileService;
import sinchonthon4.demo.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/networking/profiles")
@RequiredArgsConstructor
public class NetworkingProfileController {

    private final NetworkingProfileService networkingProfileService;

    @GetMapping
    public ResponseEntity<ApiResponse<NetworkingProfilePageResponse>> getProfiles(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Long skillId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        NetworkingProfileSearchCondition condition = new NetworkingProfileSearchCondition(
                name, school, major, position, skillId, page, size
        );
        NetworkingProfilePageResponse response = networkingProfileService.getProfiles(condition);

        return ResponseEntity.ok(ApiResponse.success(200, "네트워킹 프로필 목록을 조회했습니다.", response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<NetworkingProfileDetailResponse>> getProfile(@PathVariable Long userId) {
        NetworkingProfileDetailResponse response = networkingProfileService.getProfile(userId);

        return ResponseEntity.ok(ApiResponse.success(200, "네트워킹 프로필을 조회했습니다.", response));
    }
}
