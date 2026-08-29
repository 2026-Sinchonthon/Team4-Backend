package sinchonthon4.demo.domain.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.group.dto.CreateGroupRequest;
import sinchonthon4.demo.domain.group.entity.Group;
import sinchonthon4.demo.domain.group.entity.GroupCategory;
import sinchonthon4.demo.domain.group.entity.GroupMember;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberRole;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberStatus;
import sinchonthon4.demo.domain.group.entity.enums.GroupStatus;
import sinchonthon4.demo.domain.group.repository.GroupCategoryRepository;
import sinchonthon4.demo.domain.group.repository.GroupMemberRepository;
import sinchonthon4.demo.domain.group.repository.GroupRepository;
import sinchonthon4.demo.domain.group.service.GroupService;
import sinchonthon4.demo.domain.user.entity.User;
import sinchonthon4.demo.domain.user.entity.UserRole;
import sinchonthon4.demo.domain.user.repository.UserRepository;
import sinchonthon4.demo.global.auth.AuthenticatedUser;
import sinchonthon4.demo.global.auth.JwtTokenProvider;
import sinchonthon4.demo.support.JwtTestProperties;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GroupApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupCategoryRepository groupCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User owner;
    private User member;
    private User anotherUser;
    private GroupCategory category;
    private GroupCategory anotherCategory;
    private String ownerToken;
    private String memberToken;
    private String anotherToken;
    private LocalDateTime meetingAt;
    private LocalDateTime applicationDeadline;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        JwtTestProperties.register(registry);
    }

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString();
        owner = saveUser("owner-" + suffix + "@example.com", "모임장");
        member = saveUser("member-" + suffix + "@example.com", "신청자");
        anotherUser = saveUser("another-" + suffix + "@example.com", "다른 사용자");
        category = groupCategoryRepository.save(
                GroupCategory.builder().name("테스트-" + suffix).build());
        anotherCategory = groupCategoryRepository.save(
                GroupCategory.builder().name("변경-" + suffix).build());
        ownerToken = token(owner);
        memberToken = token(member);
        anotherToken = token(anotherUser);
        meetingAt = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
        applicationDeadline = meetingAt.minusDays(2);
    }

    @Test
    void joinCreatesPendingAndOwnerCanApprove() throws Exception {
        Long groupId = createGroup(3);

        mockMvc.perform(post("/api/groups/{groupId}/join", groupId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        GroupMember pending = membership(groupId, member.getId());
        assertThat(pending.getRole()).isEqualTo(GroupMemberRole.MEMBER);
        assertThat(pending.getStatus()).isEqualTo(GroupMemberStatus.PENDING);

        mockMvc.perform(get("/api/groups/{groupId}/members/pending", groupId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].memberId").value(pending.getId()))
                .andExpect(jsonPath("$.data[0].userId").value(member.getId()))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));

        mockMvc.perform(get("/api/groups/{groupId}/members", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].role").value("OWNER"));

        mockMvc.perform(patch("/api/groups/{groupId}/members/{memberId}/approve",
                        groupId, pending.getId())
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());

        assertThat(membership(groupId, member.getId()).getStatus())
                .isEqualTo(GroupMemberStatus.APPROVED);
        mockMvc.perform(get("/api/groups/{groupId}/members", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
        mockMvc.perform(get("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentMembers").value(2))
                .andExpect(jsonPath("$.data.isOwner").value(false))
                .andExpect(jsonPath("$.data.myMemberStatus").value("APPROVED"));
    }

    @Test
    void rejectAllowsOnlyPendingAndRejectedMemberCannotReapplyUntilLeaving() throws Exception {
        Long groupId = createGroup(3);
        join(groupId, memberToken);
        GroupMember pending = membership(groupId, member.getId());

        mockMvc.perform(patch("/api/groups/{groupId}/members/{memberId}/reject",
                        groupId, pending.getId())
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());
        assertThat(membership(groupId, member.getId()).getStatus())
                .isEqualTo(GroupMemberStatus.REJECTED);

        mockMvc.perform(patch("/api/groups/{groupId}/members/{memberId}/reject",
                        groupId, pending.getId())
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_MEMBER_NOT_PENDING"));
        mockMvc.perform(post("/api/groups/{groupId}/join", groupId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_ALREADY_JOINED"));

        mockMvc.perform(delete("/api/groups/{groupId}/join", groupId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk());
        assertThat(groupMemberRepository.findByGroupIdAndUserId(groupId, member.getId())).isEmpty();
    }

    @Test
    void joinValidatesRecruitingDeadlineDuplicateAndOwner() throws Exception {
        Long groupId = createGroup(3);

        mockMvc.perform(post("/api/groups/{groupId}/join", groupId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_ALREADY_JOINED"));

        join(groupId, memberToken);
        mockMvc.perform(post("/api/groups/{groupId}/join", groupId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_ALREADY_JOINED"));

        Group group = groupRepository.findById(groupId).orElseThrow();
        group.changeStatus(GroupStatus.CLOSED);
        mockMvc.perform(post("/api/groups/{groupId}/join", groupId)
                        .header("Authorization", bearer(anotherToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_NOT_RECRUITING"));

        Long expiredGroupId = createGroup(3, LocalDateTime.now().minusMinutes(1));
        mockMvc.perform(post("/api/groups/{groupId}/join", expiredGroupId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_APPLICATION_CLOSED"));
    }

    @Test
    void leavingApprovedMembershipUpdatesCountAndOwnerCannotLeave() throws Exception {
        Long groupId = createGroup(3);
        join(groupId, memberToken);
        GroupMember pending = membership(groupId, member.getId());
        approve(groupId, pending.getId());

        mockMvc.perform(delete("/api/groups/{groupId}/join", groupId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/groups/{groupId}", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentMembers").value(1));

        mockMvc.perform(delete("/api/groups/{groupId}/join", groupId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_FORBIDDEN"));
    }

    @Test
    void pendingListRequiresAuthenticationAndOwnerPermission() throws Exception {
        Long groupId = createGroup(3);

        mockMvc.perform(get("/api/groups/{groupId}/members/pending", groupId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.errorCode").value("AUTHENTICATION_REQUIRED"));
        mockMvc.perform(get("/api/groups/{groupId}/members/pending", groupId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_FORBIDDEN"));
    }

    @Test
    void approveValidatesOwnerGroupMembershipStatusAndCapacity() throws Exception {
        Long fullGroupId = createGroup(1);
        join(fullGroupId, memberToken);
        GroupMember pending = membership(fullGroupId, member.getId());

        mockMvc.perform(patch("/api/groups/{groupId}/members/{memberId}/approve",
                        fullGroupId, pending.getId())
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_FORBIDDEN"));
        mockMvc.perform(patch("/api/groups/{groupId}/members/{memberId}/approve",
                        fullGroupId, pending.getId())
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_FULL"));

        Long otherGroupId = createGroup(3);
        mockMvc.perform(patch("/api/groups/{groupId}/members/{memberId}/approve",
                        otherGroupId, pending.getId())
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_MEMBER_NOT_FOUND"));
    }

    @Test
    void ownerUpdatesOnlyTitleAndDetailKeepsOtherFields() throws Exception {
        Long groupId = createGroup(5);

        mockMvc.perform(patch("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"변경된 제목"}
                                """))
                .andExpect(status().isOk());

        Group updated = groupRepository.findWithCategoryById(groupId).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("변경된 제목");
        assertThat(updated.getDescription()).isEqualTo("기존 설명");
        assertThat(updated.getCategory().getId()).isEqualTo(category.getId());
        assertThat(updated.getLocation()).isEqualTo("신촌");
        assertThat(updated.getMeetingAt()).isEqualTo(meetingAt);
        assertThat(updated.getApplicationDeadline()).isEqualTo(applicationDeadline);
        assertThat(updated.getMaxMembers()).isEqualTo(5);
        assertThat(updated.getOpenChatUrl()).isEqualTo("https://open.example.com");

        mockMvc.perform(get("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("변경된 제목"))
                .andExpect(jsonPath("$.data.description").value("기존 설명"))
                .andExpect(jsonPath("$.data.category").value(category.getName()))
                .andExpect(jsonPath("$.data.location").value("신촌"))
                .andExpect(jsonPath("$.data.maxMembers").value(5))
                .andExpect(jsonPath("$.data.openChatUrl").value("https://open.example.com"))
                .andExpect(jsonPath("$.data.isOwner").value(true));
    }

    @Test
    void updateValidatesOwnerGroupAndCategory() throws Exception {
        Long groupId = createGroup(5);

        mockMvc.perform(patch("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"권한 없음\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_FORBIDDEN"));
        mockMvc.perform(patch("/api/groups/{groupId}", Long.MAX_VALUE)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"없는 모임\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_NOT_FOUND"));
        mockMvc.perform(patch("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":" + Long.MAX_VALUE + "}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_CATEGORY_NOT_FOUND"));
    }

    @Test
    void ownerUpdatesOnlyCategory() throws Exception {
        Long groupId = createGroup(5);

        mockMvc.perform(patch("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":" + anotherCategory.getId() + "}"))
                .andExpect(status().isOk());

        Group updated = groupRepository.findWithCategoryById(groupId).orElseThrow();
        assertThat(updated.getCategory().getId()).isEqualTo(anotherCategory.getId());
        assertThat(updated.getTitle()).isEqualTo("기존 제목");
        mockMvc.perform(get("/api/groups/{groupId}", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.category").value(anotherCategory.getName()));
    }

    @Test
    void updateRejectsMaxMembersBelowApprovedCountAndNonPositiveValue() throws Exception {
        Long groupId = createGroup(3);
        join(groupId, memberToken);
        approve(groupId, membership(groupId, member.getId()).getId());

        mockMvc.perform(patch("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxMembers\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_FULL"));
        mockMvc.perform(patch("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxMembers\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("INVALID_INPUT"));
    }

    @Test
    void updateValidatesDatesAgainstFinalEntityState() throws Exception {
        Long groupId = createGroup(5);
        String deadlineAfterMeeting = meetingAt.plusHours(1).toString();
        String meetingBeforeDeadline = applicationDeadline.minusHours(1).toString();

        mockMvc.perform(patch("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"applicationDeadline\":\"" + deadlineAfterMeeting + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("INVALID_INPUT"));
        mockMvc.perform(patch("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"meetingAt\":\"" + meetingBeforeDeadline + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("INVALID_INPUT"));
    }

    @Test
    void ownerCanChangeStatusInBothDirections() throws Exception {
        Long groupId = createGroup(5);

        updateStatus(groupId, "CLOSED");
        mockMvc.perform(get("/api/groups/{groupId}", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        updateStatus(groupId, "RECRUITING");
        mockMvc.perform(get("/api/groups/{groupId}", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECRUITING"));
    }

    @Test
    void updateRejectsEmptyStringAndInvalidEnum() throws Exception {
        Long groupId = createGroup(5);

        mockMvc.perform(patch("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("INVALID_INPUT"));
        mockMvc.perform(patch("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("INVALID_INPUT"));
    }

    @Test
    void ownerDeletesGroupAndAllMembershipRows() throws Exception {
        Long groupId = createGroup(4);
        join(groupId, memberToken);
        approve(groupId, membership(groupId, member.getId()).getId());
        join(groupId, anotherToken);

        mockMvc.perform(delete("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("모임이 삭제되었습니다."));

        assertThat(groupRepository.findById(groupId)).isEmpty();
        assertThat(groupMemberRepository.findByGroupIdAndUserId(groupId, owner.getId())).isEmpty();
        assertThat(groupMemberRepository.findByGroupIdAndUserId(groupId, member.getId())).isEmpty();
        assertThat(groupMemberRepository.findByGroupIdAndUserId(groupId, anotherUser.getId())).isEmpty();
        mockMvc.perform(get("/api/groups/{groupId}", groupId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_NOT_FOUND"));
    }

    @Test
    void memberCannotDeleteGroup() throws Exception {
        Long groupId = createGroup(3);
        join(groupId, memberToken);
        approve(groupId, membership(groupId, member.getId()).getId());

        mockMvc.perform(delete("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_FORBIDDEN"));

        assertThat(groupRepository.findById(groupId)).isPresent();
        assertThat(groupMemberRepository.findByGroupIdAndUserId(groupId, member.getId())).isPresent();
    }

    @Test
    void deletingMissingGroupReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/groups/{groupId}", Long.MAX_VALUE)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value("GROUP_NOT_FOUND"));
    }

    @Test
    void myGroupsReturnsOwnerApprovedAndPendingOnlyInMeetingOrder() throws Exception {
        LocalDateTime baseMeetingAt = LocalDateTime.now()
                .plusDays(20)
                .truncatedTo(ChronoUnit.SECONDS);
        Long ownedGroupId = createGroupFor(
                member, "내가 만든 모임", baseMeetingAt.plusDays(1), 5);
        Long pendingGroupId = createGroupFor(
                owner, "신청 중인 모임", baseMeetingAt.plusDays(2), 5);
        groupService.joinGroup(pendingGroupId, member.getId());
        Long approvedGroupId = createGroupFor(
                owner, "참가 중인 모임", baseMeetingAt.plusDays(3), 5);
        groupService.joinGroup(approvedGroupId, member.getId());
        groupService.approveMember(
                approvedGroupId,
                membership(approvedGroupId, member.getId()).getId(),
                owner.getId());
        Long rejectedGroupId = createGroupFor(
                owner, "거절된 모임", baseMeetingAt.plusDays(4), 5);
        groupService.joinGroup(rejectedGroupId, member.getId());
        groupService.rejectMember(
                rejectedGroupId,
                membership(rejectedGroupId, member.getId()).getId(),
                owner.getId());
        createGroupFor(
                anotherUser, "다른 사용자 모임", baseMeetingAt.plusDays(5), 5);

        mockMvc.perform(get("/api/groups/me")
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].id").value(ownedGroupId))
                .andExpect(jsonPath("$.data[0].title").value("내가 만든 모임"))
                .andExpect(jsonPath("$.data[0].category").value(category.getName()))
                .andExpect(jsonPath("$.data[0].location").value("신촌"))
                .andExpect(jsonPath("$.data[0].currentMembers").value(1))
                .andExpect(jsonPath("$.data[0].maxMembers").value(5))
                .andExpect(jsonPath("$.data[0].groupStatus").value("RECRUITING"))
                .andExpect(jsonPath("$.data[0].myRole").value("OWNER"))
                .andExpect(jsonPath("$.data[0].myMemberStatus").value("APPROVED"))
                .andExpect(jsonPath("$.data[1].id").value(pendingGroupId))
                .andExpect(jsonPath("$.data[1].currentMembers").value(1))
                .andExpect(jsonPath("$.data[1].myRole").value("MEMBER"))
                .andExpect(jsonPath("$.data[1].myMemberStatus").value("PENDING"))
                .andExpect(jsonPath("$.data[2].id").value(approvedGroupId))
                .andExpect(jsonPath("$.data[2].currentMembers").value(2))
                .andExpect(jsonPath("$.data[2].myRole").value("MEMBER"))
                .andExpect(jsonPath("$.data[2].myMemberStatus").value("APPROVED"));
    }

    @Test
    void myGroupsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/groups/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.errorCode").value("AUTHENTICATION_REQUIRED"));
    }

    private User saveUser(String email, String name) {
        return userRepository.save(User.create(email, "x".repeat(60), name));
    }

    private String token(User user) {
        return jwtTokenProvider.createAccessToken(
                new AuthenticatedUser(user.getId(), UserRole.USER));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private Long createGroup(int maxMembers) {
        return createGroup(maxMembers, applicationDeadline);
    }

    private Long createGroup(int maxMembers, LocalDateTime deadline) {
        return groupService.createGroup(new CreateGroupRequest(
                "기존 제목",
                "기존 설명",
                category.getId(),
                "신촌",
                meetingAt,
                deadline,
                maxMembers,
                "https://open.example.com"
        ), owner.getId());
    }

    private Long createGroupFor(User creator, String title,
                                LocalDateTime groupMeetingAt, int maxMembers) {
        return groupService.createGroup(new CreateGroupRequest(
                title,
                "내 모임 조회 테스트",
                category.getId(),
                "신촌",
                groupMeetingAt,
                groupMeetingAt.minusDays(2),
                maxMembers,
                "https://open.example.com"
        ), creator.getId());
    }

    private void join(Long groupId, String token) throws Exception {
        mockMvc.perform(post("/api/groups/{groupId}/join", groupId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isCreated());
    }

    private void approve(Long groupId, Long memberId) throws Exception {
        mockMvc.perform(patch("/api/groups/{groupId}/members/{memberId}/approve", groupId, memberId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());
    }

    private void updateStatus(Long groupId, String statusValue) throws Exception {
        mockMvc.perform(patch("/api/groups/{groupId}", groupId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + statusValue + "\"}"))
                .andExpect(status().isOk());
    }

    private GroupMember membership(Long groupId, Long userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId).orElseThrow();
    }
}
