package sinchonthon4.demo.global.auth;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import sinchonthon4.demo.domain.user.entity.UserRole;

public record AuthenticatedUser(
        Long userId,
        UserRole role
) {

    public List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
