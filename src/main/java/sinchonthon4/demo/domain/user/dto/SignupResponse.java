package sinchonthon4.demo.domain.user.dto;

import sinchonthon4.demo.domain.user.entity.User;
import sinchonthon4.demo.domain.user.entity.UserRole;

public record SignupResponse(
        Long userId,
        String email,
        String name,
        UserRole role
) {

    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getName(), user.getRole());
    }
}
