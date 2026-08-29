package sinchonthon4.demo.global.exception;

import lombok.Getter;

/**
 * 오류 응답의 data 필드에 담기는 상세 정보.
 * ApiResponse.code 는 HTTP 상태(int)를 담으므로, 프론트가 분기에 사용할 문자열 Error Code 는 여기서 제공한다.
 */
@Getter
public class ErrorResponse {

    private final String errorCode;

    private ErrorResponse(String errorCode) {
        this.errorCode = errorCode;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name());
    }
}
