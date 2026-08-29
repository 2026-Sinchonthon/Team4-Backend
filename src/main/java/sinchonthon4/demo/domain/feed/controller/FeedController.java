package sinchonthon4.demo.domain.feed.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sinchonthon4.demo.domain.feed.dto.FeedPageResponse;
import sinchonthon4.demo.domain.feed.service.FeedService;
import sinchonthon4.demo.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<ApiResponse<FeedPageResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        FeedPageResponse response = feedService.getFeed(page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "통합 피드를 조회했습니다.", response));
    }
}
