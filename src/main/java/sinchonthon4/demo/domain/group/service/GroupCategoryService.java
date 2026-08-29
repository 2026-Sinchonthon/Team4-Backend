package sinchonthon4.demo.domain.group.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.group.dto.GroupCategoryResponse;
import sinchonthon4.demo.domain.group.repository.GroupCategoryRepository;

/** 모임 카테고리 조회 Service. */
@Service
@RequiredArgsConstructor
public class GroupCategoryService {

    private final GroupCategoryRepository groupCategoryRepository;

    @Transactional(readOnly = true)
    public List<GroupCategoryResponse> getCategories() {
        return groupCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(GroupCategoryResponse::from)
                .toList();
    }
}
