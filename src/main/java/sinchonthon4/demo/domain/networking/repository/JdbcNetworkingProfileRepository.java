package sinchonthon4.demo.domain.networking.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import sinchonthon4.demo.domain.networking.dto.NetworkingProfileSearchCondition;

@Repository
@RequiredArgsConstructor
public class JdbcNetworkingProfileRepository implements NetworkingProfileRepository {

    private static final String SELECT_PROFILE = """
            SELECT p.id AS profile_id,
                   u.id AS user_id,
                   u.name,
                   p.profile_image_url,
                   p.school,
                   p.major,
                   p.grade,
                   p.position,
                   p.introduction,
                   p.github_url,
                   p.linkedin_url,
                   p.portfolio_url
            FROM profiles p
            JOIN users u ON u.id = p.user_id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public NetworkingProfilePage findAll(NetworkingProfileSearchCondition condition) {
        QueryParts queryParts = buildSearchQuery(condition);
        String selectSql = SELECT_PROFILE + queryParts.whereClause()
                + " ORDER BY p.id DESC LIMIT :limit OFFSET :offset";
        MapSqlParameterSource parameters = copyParameters(queryParts.parameters())
                .addValue("limit", condition.size())
                .addValue("offset", (long) condition.page() * condition.size());

        List<NetworkingProfileRecord> profiles = jdbcTemplate.query(selectSql, parameters, this::mapProfile);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM profiles p JOIN users u ON u.id = p.user_id " + queryParts.whereClause(),
                queryParts.parameters(),
                Long.class
        );

        return new NetworkingProfilePage(withSkills(profiles), count == null ? 0L : count);
    }

    @Override
    public Optional<NetworkingProfileRecord> findByUserId(Long userId) {
        List<NetworkingProfileRecord> profiles = jdbcTemplate.query(
                SELECT_PROFILE + " WHERE u.id = :userId",
                new MapSqlParameterSource("userId", userId),
                this::mapProfile
        );
        return profiles.isEmpty() ? Optional.empty() : Optional.of(withSkills(profiles).getFirst());
    }

    private QueryParts buildSearchQuery(NetworkingProfileSearchCondition condition) {
        List<String> predicates = new ArrayList<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        addContainsCondition(predicates, parameters, "name", "u.name", condition.name());
        addExactCondition(predicates, parameters, "school", "p.school", condition.school());
        addExactCondition(predicates, parameters, "major", "p.major", condition.major());
        addExactCondition(predicates, parameters, "position", "p.position", condition.position());

        if (condition.skillId() != null) {
            predicates.add("""
                    EXISTS (
                        SELECT 1 FROM profile_skills ps
                        WHERE ps.profile_id = p.id AND ps.skill_id = :skillId
                    )
                    """);
            parameters.addValue("skillId", condition.skillId());
        }

        String whereClause = predicates.isEmpty() ? "" : " WHERE " + String.join(" AND ", predicates);
        return new QueryParts(whereClause, parameters);
    }

    private MapSqlParameterSource copyParameters(MapSqlParameterSource original) {
        MapSqlParameterSource copy = new MapSqlParameterSource();
        for (String parameterName : original.getParameterNames()) {
            copy.addValue(parameterName, original.getValue(parameterName));
        }
        return copy;
    }

    private void addContainsCondition(List<String> predicates, MapSqlParameterSource parameters,
                                      String parameterName, String column, String value) {
        if (hasText(value)) {
            predicates.add("LOWER(" + column + ") LIKE LOWER(:" + parameterName + ")");
            parameters.addValue(parameterName, "%" + value.trim() + "%");
        }
    }

    private void addExactCondition(List<String> predicates, MapSqlParameterSource parameters,
                                   String parameterName, String column, String value) {
        if (hasText(value)) {
            predicates.add(column + " = :" + parameterName);
            parameters.addValue(parameterName, value.trim());
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private List<NetworkingProfileRecord> withSkills(List<NetworkingProfileRecord> profiles) {
        if (profiles.isEmpty()) {
            return List.of();
        }

        List<Long> profileIds = profiles.stream().map(NetworkingProfileRecord::profileId).toList();
        Map<Long, List<String>> skillsByProfileId = new HashMap<>();
        RowCallbackHandler skillCollector = resultSet -> skillsByProfileId
                .computeIfAbsent(resultSet.getLong("profile_id"), ignored -> new ArrayList<>())
                .add(resultSet.getString("name"));

        jdbcTemplate.query(
                """
                SELECT ps.profile_id, s.name
                FROM profile_skills ps
                JOIN skills s ON s.id = ps.skill_id
                WHERE ps.profile_id IN (:profileIds)
                ORDER BY s.name
                """,
                new MapSqlParameterSource("profileIds", profileIds),
                skillCollector
        );

        return profiles.stream()
                .map(profile -> profile.withSkills(
                        skillsByProfileId.getOrDefault(profile.profileId(), Collections.emptyList())
                ))
                .toList();
    }

    private NetworkingProfileRecord mapProfile(ResultSet resultSet, int rowNumber) throws SQLException {
        return new NetworkingProfileRecord(
                resultSet.getLong("profile_id"),
                resultSet.getLong("user_id"),
                resultSet.getString("name"),
                resultSet.getString("profile_image_url"),
                resultSet.getString("school"),
                resultSet.getString("major"),
                resultSet.getObject("grade", Integer.class),
                resultSet.getString("position"),
                resultSet.getString("introduction"),
                resultSet.getString("github_url"),
                resultSet.getString("linkedin_url"),
                resultSet.getString("portfolio_url"),
                List.of()
        );
    }

    private record QueryParts(String whereClause, MapSqlParameterSource parameters) {
    }
}
