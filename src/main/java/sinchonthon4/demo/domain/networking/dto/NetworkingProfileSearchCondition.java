package sinchonthon4.demo.domain.networking.dto;

public record NetworkingProfileSearchCondition(
        String name,
        String school,
        String major,
        String position,
        Long skillId,
        int page,
        int size
) {
}
