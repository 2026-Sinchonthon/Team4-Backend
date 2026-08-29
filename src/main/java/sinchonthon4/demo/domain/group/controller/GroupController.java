package sinchonthon4.demo.domain.group.controller;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sinchonthon4.demo.domain.group.dto.CreateGroupRequest;
import sinchonthon4.demo.domain.group.dto.GroupDetailResponse;
import sinchonthon4.demo.domain.group.dto.GroupSummaryResponse;
import sinchonthon4.demo.domain.group.entity.enums.GroupStatus;
import sinchonthon4.demo.domain.group.service.GroupService;
import sinchonthon4.demo.dto.response.ApiResponse;

/**
 * 모임 API.
 * 인증(Security/JWT)이 아직 도입되지 않아, 현재 사용자 식별자는 임시로 X-User-Id 헤더로 전달받는다.
 * Security 도입 후에는 이 헤더 대신 SecurityContext 값을 Service 로 넘기도록 교체한다.
 */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /** 모임 생성. 생성자는 인증 사용자에서 결정되며, Request Body 로 받지 않는다. */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @RequestHeader("X-User-Id") Long currentUserId) {
        Long groupId = groupService.createGroup(request, currentUserId);
        return ResponseEntity
                .created(URI.create("/api/groups/" + groupId))
                .body(ApiResponse.success(201, "모임이 생성되었습니다."));
    }

    /** 모임 목록 조회. 필터: categoryId, status, keyword, page, size */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GroupSummaryResponse>>> getGroups(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) GroupStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GroupSummaryResponse> result = groupService.getGroups(categoryId, status, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(200, "모임 목록 조회 성공", result));
    }

    /** 모임 상세 조회. X-User-Id 헤더가 있으면 isOwner / myMemberStatus 를 함께 계산한다(선택). */
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDetailResponse>> getGroup(
            @PathVariable Long groupId,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        GroupDetailResponse result = groupService.getGroup(groupId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(200, "모임 상세 조회 성공", result));
    }
}
