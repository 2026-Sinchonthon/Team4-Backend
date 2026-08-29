package sinchonthon4.demo.domain.group.dto;

import java.time.LocalDateTime;
import sinchonthon4.demo.domain.group.entity.Group;
import sinchonthon4.demo.domain.group.entity.GroupMember;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberRole;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberStatus;
import sinchonthon4.demo.domain.group.entity.enums.GroupStatus;

/** 로그인 사용자의 모임 참여 역할과 상태를 포함하는 응답. */
public record MyGroupResponse(
        Long id,
        String title,
        String category,
        String location,
        LocalDateTime meetingAt,
        long currentMembers,
        int maxMembers,
        GroupStatus groupStatus,
        GroupMemberRole myRole,
        GroupMemberStatus myMemberStatus
) {

    public static MyGroupResponse of(GroupMember membership, long currentMembers) {
        Group group = membership.getGroup();
        return new MyGroupResponse(
                group.getId(),
                group.getTitle(),
                group.getCategory().getName(),
                group.getLocation(),
                group.getMeetingAt(),
                currentMembers,
                group.getMaxMembers(),
                group.getStatus(),
                membership.getRole(),
                membership.getStatus()
        );
    }
}
