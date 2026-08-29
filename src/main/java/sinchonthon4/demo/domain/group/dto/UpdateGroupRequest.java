package sinchonthon4.demo.domain.group.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import sinchonthon4.demo.domain.group.entity.enums.GroupStatus;

/** 전달된(non-null) 필드만 변경하는 모임 부분 수정 요청. */
public record UpdateGroupRequest(

        @Size(min = 1, max = 100, message = "모임 제목은 1자 이상 100자 이하여야 합니다.")
        String title,

        @Size(min = 1, message = "모임 설명은 비어 있을 수 없습니다.")
        String description,

        Long categoryId,

        @Size(min = 1, max = 255, message = "장소는 1자 이상 255자 이하여야 합니다.")
        String location,

        LocalDateTime meetingAt,

        LocalDateTime applicationDeadline,

        @Min(value = 1, message = "모임 정원은 1명 이상이어야 합니다.")
        @Max(value = 1000, message = "모임 정원은 최대 1000명까지 가능합니다.")
        Integer maxMembers,

        @Size(min = 1, max = 500, message = "오픈채팅 URL은 1자 이상 500자 이하여야 합니다.")
        String openChatUrl,

        GroupStatus status
) {
}
