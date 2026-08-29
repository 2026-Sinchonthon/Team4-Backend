package sinchonthon4.demo.domain.group.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.group.entity.GroupCategory;
import sinchonthon4.demo.domain.group.repository.GroupCategoryRepository;

/**
 * 모임 카테고리 초기 데이터 삽입.
 * 카테고리는 Enum 이 아닌 DB Table 로 관리하므로, 애플리케이션 기동 시 기본 값을 보장한다.
 * 이미 존재하는 카테고리는 건너뛰어 재기동 시에도 중복 삽입되지 않는다.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class GroupCategorySeeder implements ApplicationRunner {

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "스터디",
            "프로젝트",
            "취업",
            "창업",
            "커피챗",
            "네트워킹",
            "기타"
    );

    private final GroupCategoryRepository groupCategoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String name : DEFAULT_CATEGORIES) {
            if (!groupCategoryRepository.existsByName(name)) {
                groupCategoryRepository.save(GroupCategory.builder().name(name).build());
            }
        }
    }
}
