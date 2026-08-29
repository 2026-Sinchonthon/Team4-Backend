package sinchonthon4.demo.domain.feed.dto;

import java.util.List;

public record FeedPageResponse(
        List<FeedItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public FeedPageResponse {
        content = List.copyOf(content);
    }
}
