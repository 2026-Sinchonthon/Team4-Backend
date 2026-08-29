package sinchonthon4.demo.domain.profile.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sinchonthon4.demo.domain.profile.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    boolean existsByUser_Id(Long userId);

    Optional<Profile> findByUser_Id(Long userId);
}
