package sinchonthon4.demo.domain.networking.repository;

import java.util.Optional;
import sinchonthon4.demo.domain.networking.dto.NetworkingProfileSearchCondition;

public interface NetworkingProfileRepository {

    NetworkingProfilePage findAll(NetworkingProfileSearchCondition condition);

    Optional<NetworkingProfileRecord> findByUserId(Long userId);
}
