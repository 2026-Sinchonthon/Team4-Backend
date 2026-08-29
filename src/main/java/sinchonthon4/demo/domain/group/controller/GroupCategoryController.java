package sinchonthon4.demo.domain.group.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sinchonthon4.demo.domain.group.dto.GroupCategoryResponse;
import sinchonthon4.demo.domain.group.service.GroupCategoryService;
import sinchonthon4.demo.dto.response.ApiResponse;

/** 모임 카테고리 API. */
@RestController
@RequestMapping("/api/group-categories")
@RequiredArgsConstructor
public class GroupCategoryController {

    private final GroupCategoryService groupCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupCategoryResponse>>> getCategories() {
        List<GroupCategoryResponse> categories = groupCategoryService.getCategories();
        return ResponseEntity.ok(ApiResponse.success(200, "카테고리 조회 성공", categories));
    }
}
