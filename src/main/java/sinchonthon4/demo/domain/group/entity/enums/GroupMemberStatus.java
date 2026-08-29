package sinchonthon4.demo.domain.group.entity.enums;

/**
 * 모임 참가 신청 상태.
 * 참가자 수를 계산할 때는 APPROVED 상태만 포함한다.
 */
public enum GroupMemberStatus {
    PENDING,
    APPROVED,
    REJECTED
}
