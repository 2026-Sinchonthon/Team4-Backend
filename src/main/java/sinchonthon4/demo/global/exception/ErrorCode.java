package sinchonthon4.demo.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 도메인 오류에 대한 의미 있는 Error Code.
 * code 는 API 응답에 노출되는 식별자, httpStatus 는 실제 HTTP 상태 코드다.
 * 신규 도메인 오류가 필요하면 이 Enum 에 추가한다.
 */
@Getter
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // User / Auth
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    USER_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."),

    // Group
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 모임입니다."),
    GROUP_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    GROUP_NOT_RECRUITING(HttpStatus.BAD_REQUEST, "모집 중인 모임이 아닙니다."),
    GROUP_ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참가 신청한 모임입니다."),
    GROUP_FULL(HttpStatus.CONFLICT, "모임 정원이 가득 찼습니다."),
    GROUP_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."),
    GROUP_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 참가자입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
