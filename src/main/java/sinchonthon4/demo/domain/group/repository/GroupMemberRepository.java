package sinchonthon4.demo.domain.group.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sinchonthon4.demo.domain.group.entity.GroupMember;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberStatus;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    Optional<GroupMember> findByIdAndGroupId(Long memberId, Long groupId);

    List<GroupMember> findAllByGroupIdAndStatusOrderByJoinedAtAsc(
            Long groupId, GroupMemberStatus status);

    @Query("""
            select gm from GroupMember gm
            join fetch gm.group g
            join fetch g.category
            where gm.userId = :userId and gm.status in :statuses
            order by g.meetingAt asc, g.id asc
            """)
    List<GroupMember> findMyGroups(@Param("userId") Long userId,
                                   @Param("statuses") Collection<GroupMemberStatus> statuses);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from GroupMember gm where gm.group.id = :groupId")
    int deleteAllByGroupId(@Param("groupId") Long groupId);

    /** 참가자 수 집계 시 APPROVED 상태만 포함한다. */
    long countByGroupIdAndStatus(Long groupId, GroupMemberStatus status);

    /**
     * 여러 모임의 특정 상태 참가자 수를 한 번에 집계한다(목록 조회의 N+1 방지).
     * 반환 각 행은 [groupId, count] 형태다.
     */
    @Query("""
            select gm.group.id, count(gm.id) from GroupMember gm
            where gm.group.id in :groupIds and gm.status = :status
            group by gm.group.id
            """)
    List<Object[]> countByGroupIdsAndStatus(@Param("groupIds") Collection<Long> groupIds,
                                            @Param("status") GroupMemberStatus status);
}
