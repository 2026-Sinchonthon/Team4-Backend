package sinchonthon4.demo.domain.feed.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sinchonthon4.demo.domain.feed.exception.InvalidFeedPageException;
import sinchonthon4.demo.dto.response.ApiResponse;

@RestControllerAdvice(assignableTypes = FeedController.class)
public class FeedExceptionHandler {

    @ExceptionHandler(InvalidFeedPageException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPage(InvalidFeedPageException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(400, exception.getMessage()));
    }
}
