package sinchonthon4.demo.domain.networking.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.feed.repository.FeedRepository;
import sinchonthon4.demo.domain.group.entity.Group;
import sinchonthon4.demo.domain.group.entity.GroupCategory;
import sinchonthon4.demo.domain.group.entity.GroupMember;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberRole;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberStatus;
import sinchonthon4.demo.domain.group.entity.enums.GroupStatus;
import sinchonthon4.demo.domain.group.repository.GroupCategoryRepository;
import sinchonthon4.demo.domain.group.repository.GroupMemberRepository;
import sinchonthon4.demo.domain.group.repository.GroupRepository;
import sinchonthon4.demo.domain.home.repository.HomeRepository;
import sinchonthon4.demo.domain.networking.dto.NetworkingProfileSearchCondition;
import sinchonthon4.demo.domain.profile.entity.Position;
import sinchonthon4.demo.domain.profile.entity.Profile;
import sinchonthon4.demo.domain.profile.entity.ProfileSkill;
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
class JpaReadModelIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private NetworkingProfileRepository networkingProfileRepository;
    @Autowired
    private HomeRepository homeRepository;
    @Autowired
    private FeedRepository feedRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private ProfileSkillRepository profileSkillRepository;
    @Autowired
    private GroupCategoryRepository groupCategoryRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        JwtTestProperties.register(registry);
    }

    @Test
    void readModelsMatchMergedProfileSkillAndGroupEntities() throws Exception {
        User me = saveUser("내 사용자");
        User designer = saveUser("디자이너 사용자");
        Profile myProfile = saveProfile(me, "김지태", Position.BACKEND);
        Profile designerProfile = saveProfile(designer, "김신충", Position.DESIGN);
        Skill java = skillRepository.findByName("Java").orElseGet(() -> skillRepository.save(Skill.create("Java")));
        Skill figma = skillRepository.findByName("Figma").orElseGet(() -> skillRepository.save(Skill.create("Figma")));
        profileSkillRepository.save(ProfileSkill.create(myProfile, java));
        profileSkillRepository.save(ProfileSkill.create(designerProfile, figma));

        GroupCategory study = saveCategory("스터디");
        GroupCategory coffeeChat = saveCategory("커피챗");
        Group participating = saveGroup(designer.getId(), study, "참가 중인 Spring 스터디");
        saveMember(participating, me.getId());
        saveGroup(designer.getId(), study, "추천 Spring 스터디");
        saveGroup(designer.getId(), coffeeChat, "신촌 개발자 커피챗");

        var profiles = networkingProfileRepository.findAll(
                new NetworkingProfileSearchCondition("김신", "홍익대학교", "시각디자인과",
                        "UX_UI_DESIGNER", figma.getId(), 0, 20)
        );
        var detail = networkingProfileRepository.findByUserId(designer.getId()).orElseThrow();

        assertThat(profiles.content()).hasSize(1);
        assertThat(profiles.content().getFirst().position()).isEqualTo("UX_UI_DESIGNER");
        assertThat(profiles.content().getFirst().skills()).containsExactly("Figma");
        assertThat(detail.name()).isEqualTo("김신충");

        assertThat(homeRepository.findProfile(me.getId()).orElseThrow().skills()).containsExactly("Java");
        assertThat(homeRepository.findParticipatingGroups(me.getId(), 4))
                .extracting(HomeRepository.HomeGroupSummaryRecord::title)
                .containsExactly("참가 중인 Spring 스터디");
        assertThat(homeRepository.findRecommendedProfiles(me.getId(), 4))
                .extracting(HomeRepository.HomeProfileRecord::position)
                .contains("UX_UI_DESIGNER");
        assertThat(homeRepository.findRecommendedGroups(me.getId(), 4))
                .extracting(HomeRepository.HomeRecommendedGroupRecord::title)
                .containsExactly("추천 Spring 스터디");
        assertThat(homeRepository.findNetworkingEvents(4))
                .extracting(HomeRepository.HomeNetworkingEventRecord::eventType)
                .containsExactly("COFFEE_CHAT");
        assertThat(homeRepository.findRecommendedJobPostings(4)).isEmpty();

        var feed = feedRepository.findAll(0, 20);
        assertThat(feed.content())
                .extracting(FeedRepository.FeedItemRecord::contentType)
                .contains("GROUP", "NETWORKING_EVENT");

        String authorization = bearerToken(me);
        mockMvc.perform(get("/api/networking/profiles")
                        .header("Authorization", authorization)
                        .param("name", "김신")
                        .param("position", "UX_UI_DESIGNER")
                        .param("skillId", figma.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("김신충"))
                .andExpect(jsonPath("$.data.content[0].position").value("UX_UI_DESIGNER"));

        mockMvc.perform(get("/api/networking/profiles/{userId}", designer.getId())
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(designer.getId()))
                .andExpect(jsonPath("$.data.skills[0]").value("Figma"));

        mockMvc.perform(get("/api/home").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myProfile.userId").value(me.getId()))
                .andExpect(jsonPath("$.data.networkingEvents[0].eventType").value("COFFEE_CHAT"));

        mockMvc.perform(get("/api/feed").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3));
    }

    private User saveUser(String name) {
        return userRepository.save(User.create(
                UUID.randomUUID() + "@example.com",
                "$2a$10$01234567890123456789012345678901234567890123456789012",
                name
        ));
    }

    private Profile saveProfile(User user, String nickname, Position position) {
        boolean designer = position == Position.DESIGN;
        return profileRepository.save(Profile.create(
                user,
                nickname,
                designer ? "홍익대학교" : "서강대학교",
                designer ? "시각디자인과" : "컴퓨터공학과",
                3,
                position,
                "함께 성장하고 싶습니다.",
                null,
                "https://github.com/example",
                null,
                null
        ));
    }

    private GroupCategory saveCategory(String name) {
        return groupCategoryRepository.findByName(name)
                .orElseGet(() -> groupCategoryRepository.save(GroupCategory.builder().name(name).build()));
    }

    private Group saveGroup(Long ownerId, GroupCategory category, String title) {
        return groupRepository.saveAndFlush(Group.builder()
                .ownerId(ownerId)
                .category(category)
                .title(title)
                .description("테스트 모임입니다.")
                .maxMembers(4)
                .status(GroupStatus.RECRUITING)
                .build());
    }

    private void saveMember(Group group, Long userId) {
        groupMemberRepository.saveAndFlush(GroupMember.builder()
                .group(group)
                .userId(userId)
                .role(GroupMemberRole.MEMBER)
                .status(GroupMemberStatus.APPROVED)
                .build());
    }

    private String bearerToken(User user) {
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getRole());
        return "Bearer " + jwtTokenProvider.createAccessToken(principal);
    }
}
