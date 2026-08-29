package sinchonthon4.demo.domain.group.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모임 카테고리.
 * Enum 으로 고정하지 않고 DB Table 로 관리한다. (스터디, 프로젝트, 취업 등)
 */
@Entity
@Getter
@Table(name = "group_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Builder
    private GroupCategory(String name) {
        this.name = name;
    }
}
