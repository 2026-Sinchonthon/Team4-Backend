package sinchonthon4.demo.domain.group.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.group.dto.CreateGroupRequest;
import sinchonthon4.demo.domain.group.dto.GroupDetailResponse;
import sinchonthon4.demo.domain.group.dto.GroupMemberResponse;
import sinchonthon4.demo.domain.group.dto.GroupSummaryResponse;
import sinchonthon4.demo.domain.group.dto.MyGroupResponse;
import sinchonthon4.demo.domain.group.dto.UpdateGroupRequest;
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

/** 모임 도메인 Service. 인증된 사용자 ID는 Controller의 JWT principal에서 전달받는다. */
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupCategoryRepository groupCategoryRepository;
    private final GroupMemberRepository groupMemberRepository;

    /** Group과 OWNER/APPROVED membership을 하나의 트랜잭션으로 생성한다. */
    @Transactional
    public Long createGroup(CreateGroupRequest request, Long currentUserId) {
        GroupCategory category = findCategory(request.categoryId());

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

    /** 모집 중이고 마감 전인 모임에 MEMBER/PENDING 참가 신청을 생성한다. */
    @Transactional
    public void joinGroup(Long groupId, Long currentUserId) {
        Group group = findGroup(groupId);
        if (group.getStatus() != GroupStatus.RECRUITING) {
            throw new BusinessException(ErrorCode.GROUP_NOT_RECRUITING);
        }
        if (group.getApplicationDeadline() != null
                && !group.getApplicationDeadline().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.GROUP_APPLICATION_CLOSED);
        }
        if (group.getOwnerId().equals(currentUserId)
                || groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUserId)) {
            throw new BusinessException(ErrorCode.GROUP_ALREADY_JOINED);
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .userId(currentUserId)
                .role(GroupMemberRole.MEMBER)
                .status(GroupMemberStatus.PENDING)
                .build();
        try {
            groupMemberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.GROUP_ALREADY_JOINED);
        }
    }

    /** OWNER가 아닌 현재 사용자의 membership 행을 상태와 관계없이 삭제한다. */
    @Transactional
    public void leaveGroup(Long groupId, Long currentUserId) {
        findGroup(groupId);
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_MEMBER_NOT_FOUND));
        if (member.getRole() == GroupMemberRole.OWNER) {
            throw new BusinessException(ErrorCode.GROUP_FORBIDDEN);
        }
        groupMemberRepository.delete(member);
    }

    /** APPROVED 참가자 목록. OWNER membership도 포함된다. */
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getApprovedMembers(Long groupId) {
        findGroup(groupId);
        return getMembersByStatus(groupId, GroupMemberStatus.APPROVED);
    }

    /** OWNER에게만 PENDING 신청자 목록을 반환한다. */
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getPendingMembers(Long groupId, Long currentUserId) {
        Group group = findGroup(groupId);
        validateOwner(group, currentUserId);
        return getMembersByStatus(groupId, GroupMemberStatus.PENDING);
    }

    /** Group row를 잠근 뒤 APPROVED 수를 다시 조회해 정원을 넘지 않을 때만 승인한다. */
    @Transactional
    public void approveMember(Long groupId, Long memberId, Long currentUserId) {
        Group group = findGroupForUpdate(groupId);
        validateOwner(group, currentUserId);
        GroupMember member = findMember(groupId, memberId);
        validatePendingMember(member);

        long approvedCount = countApprovedMembers(groupId);
        if (approvedCount >= group.getMaxMembers()) {
            throw new BusinessException(ErrorCode.GROUP_FULL);
        }
        member.approve();
    }

    /** OWNER가 해당 모임의 PENDING MEMBER 신청만 거절할 수 있다. */
    @Transactional
    public void rejectMember(Long groupId, Long memberId, Long currentUserId) {
        Group group = findGroup(groupId);
        validateOwner(group, currentUserId);
        GroupMember member = findMember(groupId, memberId);
        validatePendingMember(member);
        member.reject();
    }

    /** non-null 필드만 기존 값과 합성해 최종 상태를 검증한 뒤 수정한다. */
    @Transactional
    public void updateGroup(Long groupId, UpdateGroupRequest request, Long currentUserId) {
        Group group = findGroupForUpdate(groupId);
        validateOwner(group, currentUserId);

        GroupCategory category = request.categoryId() == null
                ? group.getCategory()
                : findCategory(request.categoryId());
        int maxMembers = request.maxMembers() == null
                ? group.getMaxMembers()
                : request.maxMembers();
        long approvedCount = countApprovedMembers(groupId);
        if (maxMembers < approvedCount) {
            throw new BusinessException(ErrorCode.GROUP_FULL);
        }

        LocalDateTime meetingAt = request.meetingAt() == null
                ? group.getMeetingAt()
                : request.meetingAt();
        LocalDateTime applicationDeadline = request.applicationDeadline() == null
                ? group.getApplicationDeadline()
                : request.applicationDeadline();
        if (meetingAt != null && applicationDeadline != null
                && applicationDeadline.isAfter(meetingAt)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        group.update(
                category,
                request.title() == null ? group.getTitle() : request.title(),
                request.description() == null ? group.getDescription() : request.description(),
                request.location() == null ? group.getLocation() : request.location(),
                meetingAt,
                applicationDeadline,
                maxMembers,
                request.openChatUrl() == null ? group.getOpenChatUrl() : request.openChatUrl()
        );
        if (request.status() != null) {
            group.changeStatus(request.status());
        }
    }

    /** OWNER만 잠근 모임과 모든 membership을 같은 트랜잭션에서 삭제할 수 있다. */
    @Transactional
    public void deleteGroup(Long groupId, Long currentUserId) {
        Group group = findGroupForUpdate(groupId);
        validateOwner(group, currentUserId);
        groupMemberRepository.deleteAllByGroupId(groupId);
        groupRepository.delete(group);
    }

    /** 목록의 currentMembers는 APPROVED membership만 집계한다. */
    @Transactional(readOnly = true)
    public Page<GroupSummaryResponse> getGroups(Long categoryId, GroupStatus status,
                                                String keyword, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Group> groups = groupRepository.search(categoryId, status, normalizedKeyword, pageable);
        Map<Long, Long> approvedCounts = countApprovedMembers(groups.getContent());
        return groups.map(group ->
                GroupSummaryResponse.of(group, approvedCounts.getOrDefault(group.getId(), 0L)));
    }

    /** OWNER/APPROVED/PENDING membership을 일정 오름차순으로 반환하고 REJECTED는 제외한다. */
    @Transactional(readOnly = true)
    public List<MyGroupResponse> getMyGroups(Long currentUserId) {
        List<GroupMember> memberships = groupMemberRepository
                .findMyGroups(currentUserId, List.of(
                        GroupMemberStatus.APPROVED,
                        GroupMemberStatus.PENDING));
        List<Group> groups = memberships.stream()
                .map(GroupMember::getGroup)
                .toList();
        Map<Long, Long> approvedCounts = countApprovedMembers(groups);
        return memberships.stream()
                .map(membership -> MyGroupResponse.of(
                        membership,
                        approvedCounts.getOrDefault(membership.getGroup().getId(), 0L)))
                .toList();
    }

    /** 익명 상세는 isOwner=false/myMemberStatus=null, 인증 상세는 현재 사용자 기준으로 계산한다. */
    @Transactional(readOnly = true)
    public GroupDetailResponse getGroup(Long groupId, Long currentUserId) {
        Group group = groupRepository.findWithCategoryById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        long currentMembers = countApprovedMembers(groupId);
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

    private Group findGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));
    }

    private Group findGroupForUpdate(Long groupId) {
        return groupRepository.findWithCategoryByIdForUpdate(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));
    }

    private GroupCategory findCategory(Long categoryId) {
        return groupCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_CATEGORY_NOT_FOUND));
    }

    private GroupMember findMember(Long groupId, Long memberId) {
        return groupMemberRepository.findByIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_MEMBER_NOT_FOUND));
    }

    private void validateOwner(Group group, Long currentUserId) {
        if (!group.getOwnerId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.GROUP_FORBIDDEN);
        }
    }

    private void validatePendingMember(GroupMember member) {
        if (member.getRole() != GroupMemberRole.MEMBER
                || member.getStatus() != GroupMemberStatus.PENDING) {
            throw new BusinessException(ErrorCode.GROUP_MEMBER_NOT_PENDING);
        }
    }

    private long countApprovedMembers(Long groupId) {
        return groupMemberRepository.countByGroupIdAndStatus(
                groupId, GroupMemberStatus.APPROVED);
    }

    private List<GroupMemberResponse> getMembersByStatus(
            Long groupId, GroupMemberStatus status) {
        return groupMemberRepository.findAllByGroupIdAndStatusOrderByJoinedAtAsc(groupId, status)
                .stream()
                .map(GroupMemberResponse::from)
                .toList();
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
