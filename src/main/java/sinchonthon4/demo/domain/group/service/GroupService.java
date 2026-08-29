package sinchonthon4.demo.domain.group.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.group.dto.CreateGroupRequest;
import sinchonthon4.demo.domain.group.dto.GroupDetailResponse;
import sinchonthon4.demo.domain.group.dto.GroupSummaryResponse;
import sinchonthon4.demo.domain.group.entity.Group;
import sinchonthon4.demo.domain.group.entity.GroupCategory;
import sinchonthon4.demo.domain.group.entity.GroupMember;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberRole;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberStatus;
import sinchonthon4.demo.domain.group.entity.enums.GroupStatus;
import sinchonthon4.demo.domain.group.repository.GroupCategoryRepository;
import sinchonthon4.demo.domain.group.repository.GroupMemberRepository;
import sinchonthon4.demo.domain.group.repository.GroupRepository;
import sinchonthon4.demo.global.exception.BusinessException;
import sinchonthon4.demo.global.exception.ErrorCode;

/**
 * 모임 도메인 Service.
 * 인증된 사용자 식별자는 currentUserId 인자로 전달받는다.
 * (프로젝트에 Security/JWT 가 도입되면 Controller 에서 SecurityContext 값을 넘기도록 교체하면 된다.)
 */
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupCategoryRepository groupCategoryRepository;
    private final GroupMemberRepository groupMemberRepository;

    /**
     * 모임 생성. Group 생성과 OWNER GroupMember(APPROVED) 생성을 하나의 Transaction 으로 처리한다.
     */
    @Transactional
    public Long createGroup(CreateGroupRequest request, Long currentUserId) {
        GroupCategory category = groupCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_CATEGORY_NOT_FOUND));

        Group group = Group.builder()
                .ownerId(currentUserId)
                .category(category)
                .title(request.title())
                .description(request.description())
                .location(request.location())
                .meetingAt(request.meetingAt())
                .applicationDeadline(request.applicationDeadline())
                .maxMembers(request.maxMembers())
                .status(GroupStatus.RECRUITING)
                .openChatUrl(request.openChatUrl())
                .build();
        groupRepository.save(group);

        GroupMember owner = GroupMember.builder()
                .group(group)
                .userId(currentUserId)
                .role(GroupMemberRole.OWNER)
                .status(GroupMemberStatus.APPROVED)
                .build();
        groupMemberRepository.save(owner);

        return group.getId();
    }

    /**
     * 모임 목록 조회. 필터는 모두 선택적이며, currentMembers 는 APPROVED 참가자 수만 계산한다.
     */
    @Transactional(readOnly = true)
    public Page<GroupSummaryResponse> getGroups(Long categoryId, GroupStatus status,
                                                String keyword, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Group> groups = groupRepository.search(categoryId, status, normalizedKeyword, pageable);

        Map<Long, Long> approvedCounts = countApprovedMembers(groups.getContent());

        return groups.map(group ->
                GroupSummaryResponse.of(group, approvedCounts.getOrDefault(group.getId(), 0L)));
    }

    /**
     * 모임 상세 조회. currentUserId 가 있으면 isOwner / myMemberStatus 를 함께 계산한다.
     * currentUserId 가 null 이면(인증 미적용) 조회는 허용하되 isOwner=false, myMemberStatus=null 로 응답한다.
     */
    @Transactional(readOnly = true)
    public GroupDetailResponse getGroup(Long groupId, Long currentUserId) {
        Group group = groupRepository.findWithCategoryById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        long currentMembers = groupMemberRepository
                .countByGroupIdAndStatus(groupId, GroupMemberStatus.APPROVED);

        boolean isOwner = false;
        GroupMemberStatus myMemberStatus = null;
        if (currentUserId != null) {
            isOwner = group.getOwnerId().equals(currentUserId);
            myMemberStatus = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUserId)
                    .map(GroupMember::getStatus)
                    .orElse(null);
        }

        return GroupDetailResponse.of(group, currentMembers, isOwner, myMemberStatus);
    }

    private Map<Long, Long> countApprovedMembers(List<Group> groups) {
        if (groups.isEmpty()) {
            return Map.of();
        }
        List<Long> groupIds = groups.stream().map(Group::getId).toList();
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : groupMemberRepository
                .countByGroupIdsAndStatus(groupIds, GroupMemberStatus.APPROVED)) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }
}
