package sinchonthon4.demo.domain.user.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.user.dto.LoginRequest;
import sinchonthon4.demo.domain.user.dto.LoginResponse;
import sinchonthon4.demo.domain.user.dto.SignupRequest;
import sinchonthon4.demo.domain.user.dto.SignupResponse;
import sinchonthon4.demo.domain.user.entity.User;
import sinchonthon4.demo.domain.user.repository.UserRepository;
import sinchonthon4.demo.global.auth.AuthenticatedUser;
import sinchonthon4.demo.global.auth.JwtTokenProvider;
import sinchonthon4.demo.global.exception.BusinessException;
import sinchonthon4.demo.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validateBcryptPasswordLength(request.password(), ErrorCode.INVALID_INPUT);
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        User user = User.create(
                email,
                passwordEncoder.encode(request.password()),
                request.name().trim());

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        return SignupResponse.from(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        validateBcryptPasswordLength(request.password(), ErrorCode.AUTH_INVALID_CREDENTIALS);
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getRole());
        String accessToken = jwtTokenProvider.createAccessToken(principal);
        return LoginResponse.bearer(accessToken, jwtTokenProvider.getAccessTokenExpirationSeconds());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateBcryptPasswordLength(String password, ErrorCode errorCode) {
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new BusinessException(errorCode);
        }
    }
}
