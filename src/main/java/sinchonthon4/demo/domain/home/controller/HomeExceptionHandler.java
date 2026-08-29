package sinchonthon4.demo.domain.home.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sinchonthon4.demo.domain.home.exception.HomeProfileNotFoundException;
import sinchonthon4.demo.dto.response.ApiResponse;

@RestControllerAdvice(assignableTypes = HomeController.class)
public class HomeExceptionHandler {

    @ExceptionHandler(HomeProfileNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProfileNotFound(HomeProfileNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), exception.getMessage()));
    }
}
