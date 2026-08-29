package sinchonthon4.demo.domain.profile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sinchonthon4.demo.domain.profile.dto.SkillResponse;
import sinchonthon4.demo.domain.profile.service.SkillService;
import sinchonthon4.demo.dto.response.ApiResponse;

@Tag(name = "Skill", description = "기술 스택 API")
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @Operation(summary = "선택 가능한 기술 스택 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getSkills() {
        return ResponseEntity.ok(ApiResponse.success(
                200, "기술 스택 목록 조회 성공", skillService.getSkills()));
    }
}
