package sinchonthon4.demo.domain.feed.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.feed.dto.FeedItemResponse;
import sinchonthon4.demo.domain.feed.dto.FeedPageResponse;
import sinchonthon4.demo.domain.feed.exception.InvalidFeedPageException;
import sinchonthon4.demo.domain.feed.repository.FeedRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private static final int MAX_PAGE_SIZE = 100;

    private final FeedRepository feedRepository;

    public FeedPageResponse getFeed(int page, int size) {
        validate(page, size);
        var result = feedRepository.findAll(page, size);
        int totalPages = result.totalElements() == 0
                ? 0
                : (int) Math.ceil((double) result.totalElements() / size);

        return new FeedPageResponse(
                result.content().stream().map(FeedItemResponse::from).toList(),
                page, size, result.totalElements(), totalPages
        );
    }

    private void validate(int page, int size) {
        if (page < 0) {
            throw new InvalidFeedPageException("page는 0 이상이어야 합니다.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidFeedPageException("size는 1 이상 100 이하여야 합니다.");
        }
    }
}
