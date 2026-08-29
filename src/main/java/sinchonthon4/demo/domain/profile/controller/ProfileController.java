package sinchonthon4.demo.domain.profile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sinchonthon4.demo.domain.profile.dto.MyProfileResponse;
import sinchonthon4.demo.domain.profile.dto.ProfileOnboardingRequest;
import sinchonthon4.demo.domain.profile.dto.ProfileOnboardingResponse;
import sinchonthon4.demo.domain.profile.dto.ProfileUpdateRequest;
import sinchonthon4.demo.domain.profile.service.ProfileOnboardingService;
import sinchonthon4.demo.domain.profile.service.ProfileService;
import sinchonthon4.demo.dto.response.ApiResponse;
import sinchonthon4.demo.global.auth.AuthenticatedUser;

@Tag(name = "Profile", description = "프로필 API")
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileOnboardingService profileOnboardingService;
    private final ProfileService profileService;

    @Operation(
            summary = "최초 온보딩 프로필 등록",
            description = "인증된 사용자의 프로필과 기술 스택을 한 번에 등록합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<ProfileOnboardingResponse>> onboard(
            @Valid @RequestBody ProfileOnboardingRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ProfileOnboardingResponse response = profileOnboardingService.onboard(
                authenticatedUser.userId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "온보딩 프로필이 등록되었습니다.", response));
    }

    @Operation(
            summary = "내 프로필 조회",
            description = "인증된 사용자의 계정 정보, 프로필과 기술 스택을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        MyProfileResponse response = profileService.getMyProfile(authenticatedUser.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "내 프로필 조회 성공", response));
    }

    @Operation(
            summary = "내 프로필 수정",
            description = "인증된 사용자의 프로필과 최종 기술 스택 목록을 전체 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> updateMyProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        MyProfileResponse response = profileService.updateMyProfile(
                authenticatedUser.userId(), request);
        return ResponseEntity.ok(ApiResponse.success(200, "내 프로필이 수정되었습니다.", response));
    }
}
