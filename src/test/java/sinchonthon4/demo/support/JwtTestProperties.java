package sinchonthon4.demo.support;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.test.context.DynamicPropertyRegistry;

public final class JwtTestProperties {

    private static final String TEST_SECRET = createSecret();

    private JwtTestProperties() {
    }

    public static void register(DynamicPropertyRegistry registry) {
        registry.add("jwt.secret", () -> TEST_SECRET);
        registry.add("jwt.access-token-expiration-seconds", () -> 3600L);
    }

    private static String createSecret() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
