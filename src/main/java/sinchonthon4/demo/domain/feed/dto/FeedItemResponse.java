package sinchonthon4.demo.domain.feed.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import sinchonthon4.demo.domain.feed.repository.FeedRepository.FeedItemRecord;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FeedItemResponse(
        String contentType,
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        Boolean isJoinAvailable,
        String location,
        String employmentType,
        String deadlineLabel
) {
    public static FeedItemResponse from(FeedItemRecord item) {
        return new FeedItemResponse(
                item.contentType(), item.id(), item.title(), item.description(), item.thumbnailUrl(),
                item.joinAvailable(), item.location(), item.employmentType(), item.deadlineLabel()
        );
    }
}
