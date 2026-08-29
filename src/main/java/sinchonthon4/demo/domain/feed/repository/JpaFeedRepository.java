package sinchonthon4.demo.domain.feed.repository;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberStatus;
import sinchonthon4.demo.domain.group.entity.enums.GroupStatus;

@Repository
@RequiredArgsConstructor
public class JpaFeedRepository implements FeedRepository {

    private final EntityManager entityManager;

    @Override
    public FeedPage findAll(int page, int size) {
        int offset = page * size;
        int fetchLimit = offset + size;

        List<DatedFeedItem> items = new ArrayList<>();
        items.addAll(findGroups(fetchLimit));
        items.addAll(findJobPostings(fetchLimit));
        items.sort(Comparator.comparing(DatedFeedItem::sortTime).reversed());

        List<FeedItemRecord> content = items.stream()
                .skip(offset)
                .limit(size)
                .map(DatedFeedItem::item)
                .toList();

        long totalElements = countGroups() + countCurrentJobPostings();
        return new FeedPage(content, totalElements);
    }

    private List<DatedFeedItem> findGroups(int limit) {
        return entityManager.createQuery(
                        """
                        SELECT g.id, g.title, gc.name, COUNT(member.id),
                               g.status, g.maxMembers, g.createdAt
                        FROM Group g
                        JOIN g.category gc
                        LEFT JOIN GroupMember member
                               ON member.group = g AND member.status = :approved
                        GROUP BY g.id, g.title, gc.name, g.status, g.maxMembers, g.createdAt
                        ORDER BY g.createdAt DESC
                        """,
                        Object[].class
                )
                .setParameter("approved", GroupMemberStatus.APPROVED)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(row -> {
                    long currentMembers = (Long) row[3];
                    GroupStatus status = (GroupStatus) row[4];
                    int maxMembers = (Integer) row[5];
                    String categoryName = (String) row[2];
                    boolean networkingEvent = isNetworkingCategory(categoryName);
                    return new DatedFeedItem(
                            new FeedItemRecord(
                                    networkingEvent ? "NETWORKING_EVENT" : "GROUP",
                                    (Long) row[0], (String) row[1],
                                    categoryName + " · 인원수 " + currentMembers + "명",
                                    null,
                                    status == GroupStatus.RECRUITING && currentMembers < maxMembers,
                                    null, null, null
                            ),
                            (LocalDateTime) row[6]
                    );
                })
                .toList();
    }

    private List<DatedFeedItem> findJobPostings(int limit) {
        if (!isEntityMapped("JobPosting")) {
            return List.of();
        }

        return entityManager.createQuery(
                        """
                        SELECT posting.id, posting.title, posting.description, posting.thumbnailUrl,
                               posting.location, posting.employmentType, posting.deadline,
                               posting.createdAt
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
                .map(row -> new DatedFeedItem(
                        new FeedItemRecord(
                                "JOB_POSTING", (Long) row[0], (String) row[1],
                                (String) row[2], (String) row[3], null,
                                (String) row[4], (String) row[5],
                                deadlineLabel((LocalDate) row[6])
                        ),
                        (LocalDateTime) row[7]
                ))
                .toList();
    }

    private long countGroups() {
        return entityManager.createQuery("SELECT COUNT(g) FROM Group g", Long.class)
                .getSingleResult();
    }

    private long countCurrentJobPostings() {
        if (!isEntityMapped("JobPosting")) {
            return 0;
        }

        return entityManager.createQuery(
                        "SELECT COUNT(posting) FROM JobPosting posting WHERE posting.deadline >= :today",
                        Long.class
                )
                .setParameter("today", LocalDate.now())
                .getSingleResult();
    }

    private boolean isEntityMapped(String entityName) {
        return entityManager.getMetamodel().getEntities().stream()
                .anyMatch(entityType -> entityType.getName().equals(entityName));
    }

    private String deadlineLabel(LocalDate deadline) {
        long remainingDays = Math.max(ChronoUnit.DAYS.between(LocalDate.now(), deadline), 0);
        return "D-" + remainingDays;
    }

    private boolean isNetworkingCategory(String categoryName) {
        return "커피챗".equals(categoryName) || "네트워킹".equals(categoryName);
    }

    private record DatedFeedItem(FeedItemRecord item, LocalDateTime sortTime) {
    }
}
