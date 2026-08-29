package sinchonthon4.demo.global.auth;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sinchonthon4.demo.global.exception.ErrorCode;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityErrorResponseWriter errorResponseWriter;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null
                || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authorization.substring(BEARER_PREFIX.length()).trim();
        AuthenticatedUser authenticatedUser;
        try {
            if (accessToken.isBlank()) {
                throw new IllegalArgumentException("Access token is blank.");
            }
            authenticatedUser = jwtTokenProvider.parseAccessToken(accessToken);
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            errorResponseWriter.write(response, ErrorCode.AUTH_INVALID_TOKEN);
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        authenticatedUser.authorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        filterChain.doFilter(request, response);
    }
}
