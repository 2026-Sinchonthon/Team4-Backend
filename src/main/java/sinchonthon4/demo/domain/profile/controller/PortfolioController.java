package sinchonthon4.demo.domain.profile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sinchonthon4.demo.domain.profile.dto.PortfolioRequest;
import sinchonthon4.demo.domain.profile.dto.PortfolioResponse;
import sinchonthon4.demo.domain.profile.service.PortfolioService;
import sinchonthon4.demo.dto.response.ApiResponse;
import sinchonthon4.demo.global.auth.AuthenticatedUser;

@Tag(name = "Portfolio", description = "마이페이지 포트폴리오 이미지 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "내 포트폴리오 이미지 목록 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<PortfolioResponse>>> getMyPortfolios(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        List<PortfolioResponse> response = portfolioService.getMyPortfolios(authenticatedUser.userId());
        return ResponseEntity.ok(ApiResponse.success(200, "내 포트폴리오 이미지 목록 조회 성공", response));
    }

    @Operation(summary = "내 포트폴리오 이미지 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<PortfolioResponse>> create(
            @Valid @RequestBody PortfolioRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        PortfolioResponse response = portfolioService.create(authenticatedUser.userId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "포트폴리오 이미지가 생성되었습니다.", response));
    }

    @Operation(summary = "내 포트폴리오 이미지 수정")
    @PatchMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<PortfolioResponse>> update(
            @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        PortfolioResponse response = portfolioService.update(
                authenticatedUser.userId(), portfolioId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "포트폴리오 이미지가 수정되었습니다.", response));
    }

    @Operation(summary = "내 포트폴리오 이미지 삭제")
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long portfolioId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        portfolioService.delete(authenticatedUser.userId(), portfolioId);
        return ResponseEntity.ok(ApiResponse.success(200, "포트폴리오 이미지가 삭제되었습니다."));
    }
}
