package sinchonthon4.demo.domain.networking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sinchonthon4.demo.domain.networking.exception.InvalidNetworkingProfileSearchException;
import sinchonthon4.demo.domain.networking.exception.NetworkingProfileNotFoundException;
import sinchonthon4.demo.dto.response.ApiResponse;

@RestControllerAdvice(assignableTypes = NetworkingProfileController.class)
public class NetworkingProfileExceptionHandler {

    @ExceptionHandler(NetworkingProfileNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NetworkingProfileNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), exception.getMessage()));
    }

    @ExceptionHandler(InvalidNetworkingProfileSearchException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSearch(InvalidNetworkingProfileSearchException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), exception.getMessage()));
    }
}
