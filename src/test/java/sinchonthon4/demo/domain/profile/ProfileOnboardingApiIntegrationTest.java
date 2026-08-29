package sinchonthon4.demo.domain.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.profile.entity.Profile;
import sinchonthon4.demo.domain.profile.entity.Skill;
import sinchonthon4.demo.domain.profile.repository.ProfileRepository;
import sinchonthon4.demo.domain.profile.repository.ProfileSkillRepository;
import sinchonthon4.demo.domain.profile.repository.SkillRepository;
import sinchonthon4.demo.domain.user.entity.User;
import sinchonthon4.demo.domain.user.repository.UserRepository;
import sinchonthon4.demo.global.auth.AuthenticatedUser;
import sinchonthon4.demo.global.auth.JwtTokenProvider;
import sinchonthon4.demo.support.JwtTestProperties;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfileOnboardingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ProfileSkillRepository profileSkillRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        JwtTestProperties.register(registry);
    }

    @Test
    void authenticatedUserCanCreateProfileWithRequestedSkills() throws Exception {
        User user = createUser();
        Skill java = skillRepository.findByName("Java").orElseThrow();
        Skill spring = skillRepository.findByName("Spring").orElseThrow();

        mockMvc.perform(post("/api/profiles/onboarding")
                        .header("Authorization", bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingRequest(java.getId(), spring.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(user.getId()))
                .andExpect(jsonPath("$.data.nickname").value("길동"))
                .andExpect(jsonPath("$.data.position").value("BACKEND"))
                .andExpect(jsonPath("$.data.skills.length()").value(2));

        Profile profile = profileRepository.findByUser_Id(user.getId()).orElseThrow();
        assertThat(profile.getNickname()).isEqualTo("길동");
        assertThat(profileSkillRepository.findAllByProfile_IdOrderBySkill_IdAsc(profile.getId()))
                .extracting(profileSkill -> profileSkill.getSkill().getId())
                .containsExactlyInAnyOrder(java.getId(), spring.getId());
    }

    @Test
    void duplicateOnboardingReturnsProfileAlreadyExists() throws Exception {
        User user = createUser();
        Skill java = skillRepository.findByName("Java").orElseThrow();
        String request = onboardingRequest(java.getId());

        mockMvc.perform(post("/api/profiles/onboarding")
                        .header("Authorization", bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/profiles/onboarding")
                        .header("Authorization", bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.errorCode").value("PROFILE_ALREADY_EXISTS"));
    }

    @Test
    void missingSkillReturnsSkillNotFoundWithoutSavingProfile() throws Exception {
        User user = createUser();

        mockMvc.perform(post("/api/profiles/onboarding")
                        .header("Authorization", bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingRequest(Long.MAX_VALUE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value("SKILL_NOT_FOUND"));

        assertThat(profileRepository.existsByUser_Id(user.getId())).isFalse();
    }

    @Test
    void unauthenticatedOnboardingReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/profiles/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingRequest(1L)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.errorCode").value("AUTHENTICATION_REQUIRED"));
    }

    private User createUser() {
        return userRepository.save(User.create(
                "profile-" + UUID.randomUUID() + "@example.com",
                passwordEncoder.encode("Password123!"),
                "프로필 테스트 사용자"));
    }

    private String bearerToken(User user) {
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getRole());
        return "Bearer " + jwtTokenProvider.createAccessToken(principal);
    }

    private String onboardingRequest(Long... skillIds) {
        String ids = String.join(", ", java.util.Arrays.stream(skillIds)
                .map(String::valueOf)
                .toList());
        return """
                {
                  "nickname": "길동",
                  "school": "연세대학교",
                  "major": "컴퓨터과학과",
                  "grade": 3,
                  "position": "BACKEND",
                  "introduction": "Spring 백엔드 개발에 관심이 있습니다.",
                  "profileImageUrl": null,
                  "githubUrl": "https://github.com/example",
                  "linkedinUrl": null,
                  "portfolioUrl": null,
                  "skillIds": [%s]
                }
                """.formatted(ids);
    }
}
