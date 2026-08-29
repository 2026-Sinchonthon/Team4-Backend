package sinchonthon4.demo.domain.home.repository;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberStatus;
import sinchonthon4.demo.domain.group.entity.enums.GroupStatus;

@Repository
@RequiredArgsConstructor
public class JpaHomeRepository implements HomeRepository {

    private static final List<String> NETWORKING_CATEGORIES = List.of("커피챗", "네트워킹");

    private final EntityManager entityManager;

    @Override
    public Optional<HomeProfileRecord> findProfile(Long userId) {
        return entityManager.createQuery(
                        """
                        SELECT p.user.id, p.nickname, p.profileImageUrl, p.school, p.position
                        FROM Profile p
                        WHERE p.user.id = :userId
                        """,
                        Object[].class
                )
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst()
                .map(row -> toProfileRecord(row, findSkillNames(List.of((Long) row[0]))));
    }

    @Override
    public List<HomeGroupSummaryRecord> findParticipatingGroups(Long userId, int limit) {
        return entityManager.createQuery(
                        """
                        SELECT g.id, g.title, gc.name
                        FROM GroupMember gm
                        JOIN gm.group g
                        JOIN g.category gc
                        WHERE gm.userId = :userId AND gm.status = :approved
                        ORDER BY gm.joinedAt DESC
                        """,
                        Object[].class
                )
                .setParameter("userId", userId)
                .setParameter("approved", GroupMemberStatus.APPROVED)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(row -> new HomeGroupSummaryRecord(
                        (Long) row[0], (String) row[1], categoryCode((String) row[2])
                ))
                .toList();
    }

    @Override
    public List<HomeProfileRecord> findRecommendedProfiles(Long userId, int limit) {
        List<Object[]> profiles = entityManager.createQuery(
                        """
                        SELECT p.user.id, p.nickname, p.profileImageUrl, p.school, p.position
                        FROM Profile p
                        WHERE p.user.id <> :userId
                        ORDER BY p.id DESC
                        """,
                        Object[].class
                )
                .setParameter("userId", userId)
                .setMaxResults(limit)
                .getResultList();

        if (profiles.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = profiles.stream().map(row -> (Long) row[0]).toList();
        Map<Long, List<String>> skillNames = findSkillNames(userIds);
        return profiles.stream()
                .map(row -> toProfileRecord(row, skillNames))
                .toList();
    }

    @Override
    public List<HomeRecommendedGroupRecord> findRecommendedGroups(Long userId, int limit) {
        return entityManager.createQuery(
                        """
                        SELECT g.id, g.title, gc.name, COUNT(approved.id), g.maxMembers, g.status
                        FROM Group g
                        JOIN g.category gc
                        LEFT JOIN GroupMember approved
                               ON approved.group = g AND approved.status = :approved
                        WHERE g.status = :recruiting
                          AND gc.name NOT IN :networkingCategories
                          AND NOT EXISTS (
                              SELECT mine.id
                              FROM GroupMember mine
                              WHERE mine.group = g
                                AND mine.userId = :userId
                                AND mine.status IN :joinedStatuses
                          )
                        GROUP BY g.id, g.title, gc.name, g.maxMembers, g.status, g.createdAt
                        ORDER BY g.createdAt DESC
                        """,
                        Object[].class
                )
                .setParameter("approved", GroupMemberStatus.APPROVED)
                .setParameter("recruiting", GroupStatus.RECRUITING)
                .setParameter("networkingCategories", NETWORKING_CATEGORIES)
                .setParameter("userId", userId)
                .setParameter("joinedStatuses", List.of(
                        GroupMemberStatus.PENDING,
                        GroupMemberStatus.APPROVED
                ))
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(row -> {
                    long currentMembers = (Long) row[3];
                    int maxMembers = (Integer) row[4];
                    GroupStatus status = (GroupStatus) row[5];
                    return new HomeRecommendedGroupRecord(
                            (Long) row[0], (String) row[1], categoryCode((String) row[2]),
                            currentMembers, maxMembers,
                            status == GroupStatus.RECRUITING && currentMembers < maxMembers
                    );
                })
                .toList();
    }

    @Override
    public List<HomeNetworkingEventRecord> findNetworkingEvents(int limit) {
        return entityManager.createQuery(
                        """
                        SELECT g.id, g.title, gc.name, COUNT(member.id), g.maxMembers, g.status
                        FROM Group g
                        JOIN g.category gc
                        LEFT JOIN GroupMember member
                               ON member.group = g AND member.status = :approved
                        WHERE g.status = :recruiting AND gc.name IN :networkingCategories
                        GROUP BY g.id, g.title, gc.name, g.maxMembers, g.status, g.createdAt
                        ORDER BY g.createdAt DESC
                        """,
                        Object[].class
                )
                .setParameter("approved", GroupMemberStatus.APPROVED)
                .setParameter("recruiting", GroupStatus.RECRUITING)
                .setParameter("networkingCategories", NETWORKING_CATEGORIES)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(row -> {
                    long currentMembers = (Long) row[3];
                    int maxMembers = (Integer) row[4];
                    GroupStatus status = (GroupStatus) row[5];
                    return new HomeNetworkingEventRecord(
                            (Long) row[0], eventType((String) row[2]), (String) row[1],
                            currentMembers, maxMembers,
                            status == GroupStatus.RECRUITING && currentMembers < maxMembers
                    );
                })
                .toList();
    }

    @Override
    public List<HomeJobPostingRecord> findRecommendedJobPostings(int limit) {
        if (!isEntityMapped("JobPosting")) {
            return List.of();
        }

        return entityManager.createQuery(
                        """
                        SELECT posting.id, posting.companyName, posting.title, posting.description,
                               posting.location, posting.employmentType, posting.deadline,
                               posting.thumbnailUrl
                        FROM JobPosting posting
                        WHERE posting.deadline >= :today
                        ORDER BY posting.createdAt DESC
                        """,
                        Object[].class
                )
                .setParameter("today", LocalDate.now())
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(row -> new HomeJobPostingRecord(
                        (Long) row[0], (String) row[1], (String) row[2], (String) row[3],
                        (String) row[4], (String) row[5], deadlineLabel((LocalDate) row[6]),
                        (String) row[7]
                ))
                .toList();
    }

    private boolean isEntityMapped(String entityName) {
        return entityManager.getMetamodel().getEntities().stream()
                .anyMatch(entityType -> entityType.getName().equals(entityName));
    }

    private HomeProfileRecord toProfileRecord(Object[] profile, Map<Long, List<String>> skillNames) {
        Long userId = (Long) profile[0];
        return new HomeProfileRecord(
                userId, (String) profile[1], (String) profile[2], (String) profile[3],
                apiPosition(profile[4].toString()), skillNames.getOrDefault(userId, List.of())
        );
    }

    private Map<Long, List<String>> findSkillNames(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<String>> result = new LinkedHashMap<>();
        entityManager.createQuery(
                        """
                        SELECT profileSkill.profile.user.id, profileSkill.skill.name
                        FROM ProfileSkill profileSkill
                        WHERE profileSkill.profile.user.id IN :userIds
                        ORDER BY profileSkill.skill.name
                        """,
                        Object[].class
                )
                .setParameter("userIds", userIds)
                .getResultList()
                .forEach(row -> result.computeIfAbsent((Long) row[0], ignored -> new ArrayList<>())
                        .add((String) row[1]));
        return result;
    }

    private String deadlineLabel(LocalDate deadline) {
        long remainingDays = Math.max(ChronoUnit.DAYS.between(LocalDate.now(), deadline), 0);
        return "D-" + remainingDays;
    }

    private String categoryCode(String categoryName) {
        return switch (categoryName) {
            case "스터디" -> "STUDY";
            case "프로젝트" -> "PROJECT";
            case "취업" -> "EMPLOYMENT";
            case "창업" -> "STARTUP";
            case "커피챗" -> "COFFEE_CHAT";
            case "네트워킹" -> "NETWORKING";
            default -> "OTHER";
        };
    }

    private String eventType(String categoryName) {
        return "커피챗".equals(categoryName) ? "COFFEE_CHAT" : "NETWORKING";
    }

    private String apiPosition(String position) {
        return "DESIGN".equals(position) ? "UX_UI_DESIGNER" : position;
    }
}
