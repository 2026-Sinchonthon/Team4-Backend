package sinchonthon4.demo.domain.home.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sinchonthon4.demo.domain.home.dto.HomeResponse;
import sinchonthon4.demo.domain.home.service.HomeService;
import sinchonthon4.demo.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ResponseEntity<ApiResponse<HomeResponse>> getHome(
            @RequestParam(defaultValue = "1") Long userId
    ) {
        HomeResponse response = homeService.getHome(userId);
        return ResponseEntity.ok(ApiResponse.success(200, "홈 화면을 조회했습니다.", response));
    }
}
