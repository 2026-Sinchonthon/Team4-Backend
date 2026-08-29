package sinchonthon4.demo.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sinchonthon4.demo.domain.user.entity.User;
import sinchonthon4.demo.domain.user.entity.UserRole;
import sinchonthon4.demo.domain.user.repository.UserRepository;
import sinchonthon4.demo.dto.response.ApiResponse;
import sinchonthon4.demo.global.auth.AuthenticatedUser;
import sinchonthon4.demo.global.auth.JwtTokenProvider;
import sinchonthon4.demo.support.JwtTestProperties;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AuthApiIntegrationTest.TestAuthController.class)
class AuthApiIntegrationTest {

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "Password123!";
    private static final String SIGNUP_REQUEST = """
            {
              "email": "user@example.com",
              "password": "Password123!",
              "name": "홍길동"
            }
            """;
    private static final String LOGIN_REQUEST = """
            {
              "email": "user@example.com",
              "password": "Password123!"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        JwtTestProperties.register(registry);
    }

    @Test
    void signupStoresBcryptPasswordAndReturnsUserWithoutPassword() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SIGNUP_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.userId").isNumber())
                .andExpect(jsonPath("$.data.email").value(EMAIL))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        User savedUser = userRepository.findByEmail(EMAIL).orElseThrow();
        assertThat(savedUser.getPasswordHash()).startsWith("$2");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, savedUser.getPasswordHash())).isTrue();
    }

    @Test
    void signupRejectsDuplicateEmailIgnoringCaseAndWhitespace() throws Exception {
        signup();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "  USER@example.com  ",
                                  "password": "Different123!",
                                  "name": "다른 사용자"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.data.errorCode").value("USER_EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void signupRejectsInvalidInput() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "short",
                                  "name": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data.errorCode").value("INVALID_INPUT"));
    }

    @Test
    void loginReturnsBearerAccessTokenWithConfiguredExpiration() throws Exception {
        signup();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600))
                .andReturn();

        String accessToken = JsonPath.read(
                result.getResponse().getContentAsString(), "$.data.accessToken");
        AuthenticatedUser principal = jwtTokenProvider.parseAccessToken(accessToken);
        User savedUser = userRepository.findByEmail(EMAIL).orElseThrow();

        assertThat(principal.userId()).isEqualTo(savedUser.getId());
        assertThat(principal.role()).isEqualTo(UserRole.USER);
    }

    @Test
    void loginRejectsWrongPasswordWithoutDistinguishingAccountExistence() throws Exception {
        signup();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "WrongPassword123!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.errorCode").value("AUTH_INVALID_CREDENTIALS"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "missing@example.com",
                                  "password": "WrongPassword123!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.errorCode").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    void bearerTokenProvidesAuthenticatedUserPrincipal() throws Exception {
        signup();
        String accessToken = loginAndGetAccessToken();
        Long userId = userRepository.findByEmail(EMAIL).orElseThrow().getId();

        mockMvc.perform(get("/api/test/authenticated-user")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void protectedEndpointReturnsApiResponseForMissingInvalidAndForbiddenAuthentication() throws Exception {
        mockMvc.perform(get("/api/test/authenticated-user"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.errorCode").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(get("/api/test/authenticated-user")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.errorCode").value("AUTH_INVALID_TOKEN"));

        signup();
        String accessToken = loginAndGetAccessToken();
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.errorCode").value("AUTH_ACCESS_DENIED"));
    }

    @Test
    void openApiDocumentsAuthEndpointsAndBearerScheme() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.paths['/api/auth/signup']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists());
    }

    private void signup() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SIGNUP_REQUEST))
                .andExpect(status().isCreated());
    }

    private String loginAndGetAccessToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_REQUEST))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    @RestController
    static class TestAuthController {

        @GetMapping("/api/test/authenticated-user")
        ApiResponse<AuthenticatedUser> authenticatedUser(
                @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
            return ApiResponse.success(200, "인증 사용자 조회 성공", authenticatedUser);
        }

        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/api/test/admin")
        ApiResponse<Void> adminOnly() {
            return ApiResponse.success(200, "관리자 접근 성공");
        }
    }
}
