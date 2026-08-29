package sinchonthon4.demo.domain.group.dto;

import java.time.LocalDateTime;
import sinchonthon4.demo.domain.group.entity.Group;
import sinchonthon4.demo.domain.group.entity.enums.GroupStatus;

/**
 * 모임 목록 카드용 요약 응답.
 * currentMembers 는 APPROVED 상태 참가자 수만 포함한다(Service 에서 계산해 전달).
 */
public record GroupSummaryResponse(
        Long id,
        String title,
        String category,
        String location,
        LocalDateTime meetingAt,
        long currentMembers,
        int maxMembers,
        GroupStatus status
) {

    public static GroupSummaryResponse of(Group group, long currentMembers) {
        return new GroupSummaryResponse(
                group.getId(),
                group.getTitle(),
                group.getCategory().getName(),
                group.getLocation(),
                group.getMeetingAt(),
                currentMembers,
                group.getMaxMembers(),
                group.getStatus()
        );
    }
}
