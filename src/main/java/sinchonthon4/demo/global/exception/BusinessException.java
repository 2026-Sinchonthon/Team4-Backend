package sinchonthon4.demo.global.exception;

import lombok.Getter;

/**
 * 도메인 규칙 위반 시 발생시키는 예외.
 * Service Layer 에서 던지고 GlobalExceptionHandler 에서 ErrorCode 기반으로 응답을 만든다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
