package sinchonthon4.demo.domain.group.dto;

import java.time.LocalDateTime;
import sinchonthon4.demo.domain.group.entity.Group;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberStatus;
import sinchonthon4.demo.domain.group.entity.enums.GroupStatus;

/**
 * 모임 상세 응답. 한 화면을 그리는 데 필요한 정보를 한 번에 제공한다.
 * isOwner / myMemberStatus 는 인증 사용자 기준 값이며, 인증 정보가 없으면 각각 false / null 이다.
 */
public record GroupDetailResponse(
        Long id,
        String title,
        String description,
        String category,
        String location,
        LocalDateTime meetingAt,
        LocalDateTime applicationDeadline,
        long currentMembers,
        int maxMembers,
        GroupStatus status,
        String openChatUrl,
        Long ownerId,
        boolean isOwner,
        GroupMemberStatus myMemberStatus
) {

    public static GroupDetailResponse of(Group group, long currentMembers,
                                         boolean isOwner, GroupMemberStatus myMemberStatus) {
        return new GroupDetailResponse(
                group.getId(),
                group.getTitle(),
                group.getDescription(),
                group.getCategory().getName(),
                group.getLocation(),
                group.getMeetingAt(),
                group.getApplicationDeadline(),
                currentMembers,
                group.getMaxMembers(),
                group.getStatus(),
                group.getOpenChatUrl(),
                group.getOwnerId(),
                isOwner,
                myMemberStatus
        );
    }
}
