package sinchonthon4.demo.domain.networking.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sinchonthon4.demo.domain.networking.dto.NetworkingProfileSearchCondition;

@Repository
@RequiredArgsConstructor
public class JpaNetworkingProfileRepository implements NetworkingProfileRepository {

    private static final String PROFILE_PROJECTION = """
            SELECT p.id, p.user.id, p.user.name, p.profileImageUrl, p.school, p.major,
                   p.grade, p.position, p.introduction, p.githubUrl, p.linkedinUrl,
                   p.portfolioUrl
            FROM Profile p
            """;

    private final EntityManager entityManager;

    @Override
    public NetworkingProfilePage findAll(NetworkingProfileSearchCondition condition) {
        QueryParts queryParts = buildQueryParts(condition);
        TypedQuery<Object[]> query = entityManager.createQuery(
                PROFILE_PROJECTION + queryParts.joinClause() + queryParts.whereClause()
                        + " ORDER BY p.id DESC",
                Object[].class
        );
        applyParameters(query, queryParts.parameters());
        query.setFirstResult(condition.page() * condition.size());
        query.setMaxResults(condition.size());

        List<NetworkingProfileRecord> profiles = query.getResultList().stream()
                .map(this::toRecord)
                .toList();

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "SELECT COUNT(DISTINCT p.id) FROM Profile p "
                        + queryParts.joinClause() + queryParts.whereClause(),
                Long.class
        );
        applyParameters(countQuery, queryParts.parameters());
        return new NetworkingProfilePage(withSkills(profiles), countQuery.getSingleResult());
    }

    @Override
    public Optional<NetworkingProfileRecord> findByUserId(Long userId) {
        List<NetworkingProfileRecord> profiles = entityManager.createQuery(
                        PROFILE_PROJECTION + " WHERE p.user.id = :userId",
                        Object[].class
                )
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .map(this::toRecord)
                .toList();
        return profiles.isEmpty()
                ? Optional.empty()
                : Optional.of(withSkills(profiles).getFirst());
    }

    private QueryParts buildQueryParts(NetworkingProfileSearchCondition condition) {
        List<String> predicates = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();
        String joinClause = "";

        if (hasText(condition.name())) {
            predicates.add("LOWER(p.user.name) LIKE :name");
            parameters.put("name", "%" + condition.name().trim().toLowerCase() + "%");
        }
        if (hasText(condition.school())) {
            predicates.add("p.school = :school");
            parameters.put("school", condition.school().trim());
        }
        if (hasText(condition.major())) {
            predicates.add("p.major = :major");
            parameters.put("major", condition.major().trim());
        }
        if (hasText(condition.position())) {
            predicates.add("CAST(p.position AS string) = :position");
            parameters.put("position", condition.position().trim());
        }
        if (condition.skillId() != null) {
            joinClause = " JOIN p.skills filteredSkill ";
            predicates.add("filteredSkill.id = :skillId");
            parameters.put("skillId", condition.skillId());
        }

        String whereClause = predicates.isEmpty() ? "" : " WHERE " + String.join(" AND ", predicates);
        return new QueryParts(joinClause, whereClause, parameters);
    }

    private void applyParameters(TypedQuery<?> query, Map<String, Object> parameters) {
        parameters.forEach(query::setParameter);
    }

    private List<NetworkingProfileRecord> withSkills(List<NetworkingProfileRecord> profiles) {
        if (profiles.isEmpty()) {
            return List.of();
        }

        Map<Long, List<String>> skillsByProfileId = new HashMap<>();
        entityManager.createQuery(
                        """
                        SELECT p.id, skill.name
                        FROM Profile p
                        JOIN p.skills skill
                        WHERE p.id IN :profileIds
                        ORDER BY skill.name
                        """,
                        Object[].class
                )
                .setParameter("profileIds", profiles.stream()
                        .map(NetworkingProfileRecord::profileId)
                        .toList())
                .getResultList()
                .forEach(row -> skillsByProfileId
                        .computeIfAbsent((Long) row[0], ignored -> new ArrayList<>())
                        .add((String) row[1]));

        return profiles.stream()
                .map(profile -> profile.withSkills(
                        skillsByProfileId.getOrDefault(profile.profileId(), List.of())
                ))
                .toList();
    }

    private NetworkingProfileRecord toRecord(Object[] row) {
        return new NetworkingProfileRecord(
                (Long) row[0], (Long) row[1], (String) row[2], (String) row[3],
                (String) row[4], (String) row[5], (Integer) row[6], row[7].toString(),
                (String) row[8], (String) row[9], (String) row[10], (String) row[11],
                List.of()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record QueryParts(
            String joinClause,
            String whereClause,
            Map<String, Object> parameters
    ) {
    }
}
