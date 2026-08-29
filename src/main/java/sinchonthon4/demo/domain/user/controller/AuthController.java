package sinchonthon4.demo.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sinchonthon4.demo.domain.user.dto.LoginRequest;
import sinchonthon4.demo.domain.user.dto.LoginResponse;
import sinchonthon4.demo.domain.user.dto.SignupRequest;
import sinchonthon4.demo.domain.user.dto.SignupResponse;
import sinchonthon4.demo.domain.user.service.AuthService;
import sinchonthon4.demo.dto.response.ApiResponse;

@Tag(name = "Auth", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "이메일 회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "회원가입이 완료되었습니다.", response));
    }

    @Operation(summary = "이메일 로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(200, "로그인에 성공했습니다.", response));
    }
}
