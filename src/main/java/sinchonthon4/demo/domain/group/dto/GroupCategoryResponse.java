package sinchonthon4.demo.domain.group.dto;

import sinchonthon4.demo.domain.group.entity.GroupCategory;

/** 모임 카테고리 응답. */
public record GroupCategoryResponse(
        Long id,
        String name
) {

    public static GroupCategoryResponse from(GroupCategory category) {
        return new GroupCategoryResponse(category.getId(), category.getName());
    }
}
