package sinchonthon4.demo.domain.group.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sinchonthon4.demo.domain.group.entity.Group;
import sinchonthon4.demo.domain.group.entity.enums.GroupStatus;

public interface GroupRepository extends JpaRepository<Group, Long> {

    /**
     * 상세 조회 시 category 를 함께 로딩해 N+1 을 피한다.
     */
    @Query("select g from Group g join fetch g.category where g.id = :id")
    Optional<Group> findWithCategoryById(@Param("id") Long id);

    /**
     * 목록 검색. 모든 필터는 선택적이며 null 이면 해당 조건을 건너뛴다.
     * category 를 fetch join 하여 목록 매핑 시 카테고리 N+1 을 피한다.
     * 해커톤 범위이므로 Specification/QueryDSL 대신 단순 JPQL 로 처리한다.
     */
    @Query("""
            select g from Group g
            join fetch g.category c
            where (:categoryId is null or c.id = :categoryId)
              and (:status is null or g.status = :status)
              and (:keyword is null or g.title like %:keyword% or g.description like %:keyword%)
            """)
    Page<Group> search(@Param("categoryId") Long categoryId,
                       @Param("status") GroupStatus status,
                       @Param("keyword") String keyword,
                       Pageable pageable);
}
