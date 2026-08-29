package sinchonthon4.demo.domain.group.dto;

import java.time.LocalDateTime;
import sinchonthon4.demo.domain.group.entity.GroupMember;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberRole;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberStatus;

/** Group 도메인 정보만 노출하는 참가자 응답. */
public record GroupMemberResponse(
        Long memberId,
        Long userId,
        GroupMemberRole role,
        GroupMemberStatus status,
        LocalDateTime joinedAt
) {

    public static GroupMemberResponse from(GroupMember member) {
        return new GroupMemberResponse(
                member.getId(),
                member.getUserId(),
                member.getRole(),
                member.getStatus(),
                member.getJoinedAt()
        );
    }
}
