package sinchonthon4.demo.domain.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.Arrays;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.profile.entity.Portfolio;
import sinchonthon4.demo.domain.profile.entity.Position;
import sinchonthon4.demo.domain.profile.entity.Profile;
import sinchonthon4.demo.domain.profile.entity.ProfileSkill;
import sinchonthon4.demo.domain.profile.entity.Skill;
import sinchonthon4.demo.domain.profile.repository.PortfolioRepository;
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
class MyPageApiIntegrationTest {

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
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        JwtTestProperties.register(registry);
    }

    @Test
    void authenticatedUserCanGetOwnProfile() throws Exception {
        User user = createUser();
        Skill java = skill("Java");
        Skill spring = skill("Spring");
        createProfile(user, java, spring);

        mockMvc.perform(get("/api/profiles/me")
                        .header("Authorization", bearerToken(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(user.getId()))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                .andExpect(jsonPath("$.data.name").value(user.getName()))
                .andExpect(jsonPath("$.data.nickname").value("길동"))
                .andExpect(jsonPath("$.data.skills.length()").value(2))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    void authenticatedUserCanReplaceProfileDetailsAndSkills() throws Exception {
        User user = createUser();
        Skill java = skill("Java");
        Skill spring = skill("Spring");
        Skill mysql = skill("MySQL");
        Profile profile = createProfile(user, java);

        mockMvc.perform(patch("/api/profiles/me")
                        .header("Authorization", bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileUpdateRequest("수정된 길동", spring.getId(), mysql.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("수정된 길동"))
                .andExpect(jsonPath("$.data.grade").value(4))
                .andExpect(jsonPath("$.data.skills.length()").value(2));

        Profile updated = profileRepository.findByUser_Id(user.getId()).orElseThrow();
        assertThat(updated.getNickname()).isEqualTo("수정된 길동");
        assertThat(updated.getGrade()).isEqualTo(4);
        assertThat(profileSkillRepository.findAllByProfile_IdOrderBySkill_IdAsc(profile.getId()))
                .extracting(profileSkill -> profileSkill.getSkill().getId())
                .containsExactlyInAnyOrder(spring.getId(), mysql.getId());
    }

    @Test
    void missingSkillDoesNotPartiallyUpdateProfileOrSkills() throws Exception {
        User user = createUser();
        Skill java = skill("Java");
        Profile profile = createProfile(user, java);

        mockMvc.perform(patch("/api/profiles/me")
                        .header("Authorization", bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileUpdateRequest("변경되면 안 됨", java.getId(), Long.MAX_VALUE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value("SKILL_NOT_FOUND"));

        Profile unchanged = profileRepository.findByUser_Id(user.getId()).orElseThrow();
        assertThat(unchanged.getNickname()).isEqualTo("길동");
        assertThat(profileSkillRepository.findAllByProfile_IdOrderBySkill_IdAsc(profile.getId()))
                .extracting(profileSkill -> profileSkill.getSkill().getId())
                .containsExactly(java.getId());
    }

    @Test
    void authenticatedUserCanCreateListUpdateAndDeletePortfolio() throws Exception {
        User user = createUser();
        String firstImage = "https://example.com/portfolio-1.png";
        String secondImage = "https://example.com/portfolio-2.png";

        MvcResult createResult = mockMvc.perform(post("/api/portfolios")
                        .header("Authorization", bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(portfolioRequest("내 포트폴리오", firstImage, secondImage)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("내 포트폴리오"))
                .andExpect(jsonPath("$.data.imageUrls.length()").value(2))
                .andExpect(jsonPath("$.data.imageUrls[0]").value(firstImage))
                .andExpect(jsonPath("$.data.imageUrls[1]").value(secondImage))
                .andReturn();

        Number portfolioIdValue = JsonPath.read(
                createResult.getResponse().getContentAsString(), "$.data.portfolioId");
        Long portfolioId = portfolioIdValue.longValue();

        mockMvc.perform(get("/api/portfolios/me")
                        .header("Authorization", bearerToken(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].portfolioId").value(portfolioId))
                .andExpect(jsonPath("$.data[0].imageUrls.length()").value(2))
                .andExpect(jsonPath("$.data[0].imageUrls[0]").value(firstImage));

        String updatedImage = "https://example.com/portfolio-updated.png";
        mockMvc.perform(patch("/api/portfolios/{portfolioId}", portfolioId)
                        .header("Authorization", bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(portfolioRequest("수정된 포트폴리오", updatedImage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 포트폴리오"))
                .andExpect(jsonPath("$.data.imageUrls.length()").value(1))
                .andExpect(jsonPath("$.data.imageUrls[0]").value(updatedImage));

        mockMvc.perform(delete("/api/portfolios/{portfolioId}", portfolioId)
                        .header("Authorization", bearerToken(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        assertThat(portfolioRepository.existsById(portfolioId)).isFalse();
    }

    @Test
    void otherUserCannotUpdatePortfolio() throws Exception {
        User owner = createUser();
        User otherUser = createUser();
        String originalImage = "https://example.com/owner-portfolio.png";
        Portfolio portfolio = portfolioRepository.save(
                Portfolio.create(owner, "소유자 포트폴리오", null, java.util.List.of(originalImage)));

        mockMvc.perform(patch("/api/portfolios/{portfolioId}", portfolio.getId())
                        .header("Authorization", bearerToken(otherUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(portfolioRequest("탈취 시도", "https://example.com/unauthorized.png")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.errorCode").value("PORTFOLIO_FORBIDDEN"));

        assertThat(portfolio.getImageUrls()).containsExactly(originalImage);
    }

    private User createUser() {
        return userRepository.save(User.create(
                "mypage-" + UUID.randomUUID() + "@example.com",
                passwordEncoder.encode("Password123!"),
                "홍길동"));
    }

    private Skill skill(String name) {
        return skillRepository.findByName(name).orElseThrow();
    }

    private Profile createProfile(User user, Skill... skills) {
        Profile profile = profileRepository.save(Profile.create(
                user,
                "길동",
                "연세대학교",
                "컴퓨터과학과",
                3,
                Position.BACKEND,
                "Spring 백엔드 개발에 관심이 있습니다.",
                null,
                "https://github.com/example",
                null,
                null));
        profileSkillRepository.saveAll(Arrays.stream(skills)
                .map(skill -> ProfileSkill.create(profile, skill))
                .toList());
        return profile;
    }

    private String bearerToken(User user) {
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getRole());
        return "Bearer " + jwtTokenProvider.createAccessToken(principal);
    }

    private String profileUpdateRequest(String nickname, Long... skillIds) {
        String ids = String.join(", ", Arrays.stream(skillIds)
                .map(String::valueOf)
                .toList());
        return """
                {
                  "nickname": "%s",
                  "school": "연세대학교",
                  "major": "컴퓨터과학과",
                  "grade": 4,
                  "position": "BACKEND",
                  "introduction": "Spring 기반 프로젝트를 진행하고 있습니다.",
                  "profileImageUrl": null,
                  "githubUrl": "https://github.com/example",
                  "linkedinUrl": null,
                  "portfolioUrl": null,
                  "skillIds": [%s]
                }
                """.formatted(nickname, ids);
    }

    private String portfolioRequest(String title, String... imageUrls) {
        String urls = String.join(", ", Arrays.stream(imageUrls)
                .map("\"%s\""::formatted)
                .toList());
        return """
                {
                  "title": "%s",
                  "description": null,
                  "imageUrls": [%s]
                }
                """.formatted(title, urls);
    }
}
