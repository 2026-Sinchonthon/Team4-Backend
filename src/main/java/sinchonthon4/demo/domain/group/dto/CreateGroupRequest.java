package sinchonthon4.demo.domain.group.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 모임 생성 요청.
 * ownerId 는 인증된 사용자에서 가져오므로 Request Body 로 받지 않는다.
 */
public record CreateGroupRequest(

        @NotBlank(message = "모임 제목은 필수입니다.")
        @Size(max = 100, message = "모임 제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "모임 설명은 필수입니다.")
        String description,

        @NotNull(message = "카테고리는 필수입니다.")
        Long categoryId,

        @Size(max = 255, message = "장소는 255자 이하여야 합니다.")
        String location,

        LocalDateTime meetingAt,

        LocalDateTime applicationDeadline,

        @NotNull(message = "모임 정원은 필수입니다.")
        @Min(value = 2, message = "모임 정원은 최소 2명이어야 합니다.")
        @Max(value = 1000, message = "모임 정원은 최대 1000명까지 가능합니다.")
        Integer maxMembers,

        @Size(max = 500, message = "오픈채팅 URL은 500자 이하여야 합니다.")
        String openChatUrl
) {
}
