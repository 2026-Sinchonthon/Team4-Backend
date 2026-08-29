package sinchonthon4.demo.domain.group.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sinchonthon4.demo.domain.group.dto.CreateGroupRequest;
import sinchonthon4.demo.domain.group.dto.GroupDetailResponse;
import sinchonthon4.demo.domain.group.dto.GroupMemberResponse;
import sinchonthon4.demo.domain.group.dto.GroupSummaryResponse;
import sinchonthon4.demo.domain.group.dto.MyGroupResponse;
import sinchonthon4.demo.domain.group.dto.UpdateGroupRequest;
import sinchonthon4.demo.domain.group.entity.enums.GroupStatus;
import sinchonthon4.demo.domain.group.service.GroupService;
import sinchonthon4.demo.dto.response.ApiResponse;
import sinchonthon4.demo.global.auth.AuthenticatedUser;

@Tag(name = "Group", description = "모임 생성, 조회, 수정, 삭제 및 참가 관리 API")
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "모임 생성")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        Long groupId = groupService.createGroup(request, authenticatedUser.userId());
        return ResponseEntity
                .created(URI.create("/api/groups/" + groupId))
                .body(ApiResponse.success(201, "모임이 생성되었습니다."));
    }

    @Operation(summary = "모임 참가 신청")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{groupId}/join")
    public ResponseEntity<ApiResponse<Void>> joinGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        groupService.joinGroup(groupId, authenticatedUser.userId());
        return ResponseEntity.status(201)
                .body(ApiResponse.success(201, "참가 신청이 완료되었습니다."));
    }

    @Operation(summary = "모임 참가 신청 취소 또는 탈퇴")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{groupId}/join")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        groupService.leaveGroup(groupId, authenticatedUser.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "참가 신청 취소 또는 탈퇴가 완료되었습니다."));
    }

    @Operation(summary = "승인된 모임 참가자 조회")
    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> getApprovedMembers(
            @PathVariable Long groupId) {
        List<GroupMemberResponse> result = groupService.getApprovedMembers(groupId);
        return ResponseEntity.ok(ApiResponse.success(200, "참가자 목록 조회 성공", result));
    }

    @Operation(summary = "대기 중인 모임 참가 신청자 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{groupId}/members/pending")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> getPendingMembers(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        List<GroupMemberResponse> result = groupService.getPendingMembers(
                groupId, authenticatedUser.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "대기 신청자 목록 조회 성공", result));
    }

    @Operation(summary = "모임 참가 신청 승인")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{groupId}/members/{memberId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        groupService.approveMember(groupId, memberId, authenticatedUser.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "참가 신청이 승인되었습니다."));
    }

    @Operation(summary = "모임 참가 신청 거절")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{groupId}/members/{memberId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        groupService.rejectMember(groupId, memberId, authenticatedUser.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "참가 신청이 거절되었습니다."));
    }

    @Operation(summary = "모임 수정")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        groupService.updateGroup(groupId, request, authenticatedUser.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "모임이 수정되었습니다."));
    }

    @Operation(summary = "모임 삭제")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        groupService.deleteGroup(groupId, authenticatedUser.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "모임이 삭제되었습니다."));
    }

    @Operation(summary = "모임 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GroupSummaryResponse>>> getGroups(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) GroupStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GroupSummaryResponse> result = groupService.getGroups(categoryId, status, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "모임 목록 조회 성공", result));
    }

    @Operation(summary = "내 모임 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<MyGroupResponse>>> getMyGroups(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        List<MyGroupResponse> result = groupService.getMyGroups(authenticatedUser.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "내 모임 조회 성공", result));
    }

    @Operation(summary = "모임 상세 조회")
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDetailResponse>> getGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        Long currentUserId = authenticatedUser == null ? null : authenticatedUser.userId();
        GroupDetailResponse result = groupService.getGroup(groupId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "모임 상세 조회 성공", result));
    }
}
