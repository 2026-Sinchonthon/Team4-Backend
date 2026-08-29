package sinchonthon4.demo.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import sinchonthon4.demo.domain.user.entity.UserRole;

@Component
public class JwtTokenProvider {

    private static final String ROLE_CLAIM = "role";

    private final SecretKey signingKey;
    private final long accessTokenExpirationSeconds;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.signingKey = createSigningKey(jwtProperties.secret());
        this.accessTokenExpirationSeconds = jwtProperties.accessTokenExpirationSeconds();
    }

    public String createAccessToken(AuthenticatedUser authenticatedUser) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(accessTokenExpirationSeconds);

        return Jwts.builder()
                .subject(authenticatedUser.userId().toString())
                .claim(ROLE_CLAIM, authenticatedUser.role().name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public AuthenticatedUser parseAccessToken(String accessToken) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        String subject = claims.getSubject();
        String roleClaim = claims.get(ROLE_CLAIM, String.class);
        if (subject == null || subject.isBlank() || roleClaim == null || roleClaim.isBlank()) {
            throw new JwtException("Required access token claims are missing.");
        }

        try {
            return new AuthenticatedUser(Long.valueOf(subject), UserRole.valueOf(roleClaim));
        } catch (IllegalArgumentException e) {
            throw new JwtException("Required access token claims are invalid.", e);
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }

    private SecretKey createSigningKey(String encodedSecret) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret));
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "JWT_SECRET must be a Base64-encoded key of at least 256 bits.", e);
        }
    }
}
