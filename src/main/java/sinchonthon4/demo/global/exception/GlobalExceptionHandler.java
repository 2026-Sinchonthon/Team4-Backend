package sinchonthon4.demo.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sinchonthon4.demo.dto.response.ApiResponse;

/**
 * 전역 Exception Handler.
 * 내부 Stack Trace 나 DB 예외 메시지를 그대로 노출하지 않고, ErrorCode 기반 응답으로 변환한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(
                        errorCode.getHttpStatus().value(),
                        errorCode.getMessage(),
                        ErrorResponse.of(errorCode)));
    }

    /** Bean Validation 실패. 첫 번째 필드 오류 메시지를 대표로 내려준다. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(errorCode.getMessage());
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(
                        errorCode.getHttpStatus().value(),
                        message,
                        ErrorResponse.of(errorCode)));
    }
}
