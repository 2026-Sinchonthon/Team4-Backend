package sinchonthon4.demo.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sinchonthon4.demo.domain.feed.exception.InvalidFeedPageException;
import sinchonthon4.demo.domain.feed.repository.FeedRepository;
import sinchonthon4.demo.domain.feed.repository.FeedRepository.FeedItemRecord;
import sinchonthon4.demo.domain.feed.repository.FeedRepository.FeedPage;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @InjectMocks
    private FeedService feedService;

    @Test
    void returnsUnifiedFeedWithPagination() {
        when(feedRepository.findAll(0, 20)).thenReturn(new FeedPage(List.of(
                new FeedItemRecord(
                        "GROUP", 1L, "Spring boot 스터디", "프로젝트 · 인원수 2명",
                        null, true, null, null, null
                ),
                new FeedItemRecord(
                        "NETWORKING_EVENT", 2L, "신촌 개발자 커피챗", "커피챗 · 인원수 2명",
                        null, true, null, null, null
                ),
                new FeedItemRecord(
                        "JOB_POSTING", 3L, "Backend Developer", "스타트업 포지션 채용",
                        null, null, "서울", "정식 채용", "D-5"
                )
        ), 3));

        var response = feedService.getFeed(0, 20);

        assertThat(response.content()).hasSize(3);
        assertThat(response.content().getFirst().contentType()).isEqualTo("GROUP");
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidPageSize() {
        assertThatThrownBy(() -> feedService.getFeed(0, 101))
                .isInstanceOf(InvalidFeedPageException.class);
    }
}
