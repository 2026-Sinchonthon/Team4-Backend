package sinchonthon4.demo.domain.feed.repository;

import java.util.List;

public interface FeedRepository {

    FeedPage findAll(int page, int size);

    record FeedPage(List<FeedItemRecord> content, long totalElements) {
        public FeedPage {
            content = List.copyOf(content);
        }
    }

    record FeedItemRecord(
            String contentType,
            Long id,
            String title,
            String description,
            String thumbnailUrl,
            Boolean joinAvailable,
            String location,
            String employmentType,
            String deadlineLabel
    ) {
    }
}
