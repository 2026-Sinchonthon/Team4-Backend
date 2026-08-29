package sinchonthon4.demo.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import sinchonthon4.demo.domain.profile.entity.Skill;

@Schema(description = "기술 스택")
public record SkillResponse(
        Long id,
        String name
) {

    public static SkillResponse from(Skill skill) {
        return new SkillResponse(skill.getId(), skill.getName());
    }
}
