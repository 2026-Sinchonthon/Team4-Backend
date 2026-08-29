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
import sinchonthon4.demo.domain.networking.entity.enums.NetworkingEventStatus;
import sinchonthon4.demo.domain.networking.entity.enums.NetworkingParticipantStatus;

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
        items.addAll(findNetworkingEvents(fetchLimit));
        items.addAll(findJobPostings(fetchLimit));
        items.sort(Comparator.comparing(DatedFeedItem::sortTime).reversed());

        List<FeedItemRecord> content = items.stream()
                .skip(offset)
                .limit(size)
                .map(DatedFeedItem::item)
                .toList();

        long totalElements = countGroups() + countNetworkingEvents() + countCurrentJobPostings();
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
                    return new DatedFeedItem(
                            new FeedItemRecord(
                                    "GROUP", (Long) row[0], (String) row[1],
                                    row[2] + " · 인원수 " + currentMembers + "명",
                                    null,
                                    status == GroupStatus.RECRUITING && currentMembers < maxMembers,
                                    null, null, null
                            ),
                            (LocalDateTime) row[6]
                    );
                })
                .toList();
    }

    private List<DatedFeedItem> findNetworkingEvents(int limit) {
        return entityManager.createQuery(
                        """
                        SELECT event.id, event.title, event.eventType, event.thumbnailUrl,
                               event.location, COUNT(participant.id), event.status,
                               event.maxParticipants, event.createdAt
                        FROM NetworkingEvent event
                        LEFT JOIN NetworkingEventParticipant participant
                               ON participant.event = event AND participant.status = :approved
                        GROUP BY event.id, event.title, event.eventType, event.thumbnailUrl,
                                 event.location, event.status, event.maxParticipants, event.createdAt
                        ORDER BY event.createdAt DESC
                        """,
                        Object[].class
                )
                .setParameter("approved", NetworkingParticipantStatus.APPROVED)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(row -> {
                    long currentParticipants = (Long) row[5];
                    String eventType = row[2].toString();
                    String eventLabel = switch (eventType) {
                        case "COFFEE_CHAT" -> "커피챗";
                        case "RECRUITING_SESSION" -> "채용설명회";
                        default -> "네트워킹";
                    };
                    NetworkingEventStatus status = (NetworkingEventStatus) row[6];
                    int maxParticipants = (Integer) row[7];
                    return new DatedFeedItem(
                            new FeedItemRecord(
                                    "NETWORKING_EVENT", (Long) row[0], (String) row[1],
                                    eventLabel + " · 인원수 " + currentParticipants + "명",
                                    (String) row[3],
                                    status == NetworkingEventStatus.RECRUITING
                                            && currentParticipants < maxParticipants,
                                    (String) row[4], null, null
                            ),
                            (LocalDateTime) row[8]
                    );
                })
                .toList();
    }

    private List<DatedFeedItem> findJobPostings(int limit) {
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

    private long countNetworkingEvents() {
        return entityManager.createQuery("SELECT COUNT(event) FROM NetworkingEvent event", Long.class)
                .getSingleResult();
    }

    private long countCurrentJobPostings() {
        return entityManager.createQuery(
                        "SELECT COUNT(posting) FROM JobPosting posting WHERE posting.deadline >= :today",
                        Long.class
                )
                .setParameter("today", LocalDate.now())
                .getSingleResult();
    }

    private String deadlineLabel(LocalDate deadline) {
        long remainingDays = Math.max(ChronoUnit.DAYS.between(LocalDate.now(), deadline), 0);
        return "D-" + remainingDays;
    }

    private record DatedFeedItem(FeedItemRecord item, LocalDateTime sortTime) {
    }
}
