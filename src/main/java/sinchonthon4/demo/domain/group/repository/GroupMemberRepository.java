package sinchonthon4.demo.domain.group.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sinchonthon4.demo.domain.group.entity.GroupMember;
import sinchonthon4.demo.domain.group.entity.enums.GroupMemberStatus;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    List<GroupMember> findAllByGroupIdAndStatus(Long groupId, GroupMemberStatus status);

    /** 참가자 수 집계 시 APPROVED 상태만 포함한다. */
    long countByGroupIdAndStatus(Long groupId, GroupMemberStatus status);
}
