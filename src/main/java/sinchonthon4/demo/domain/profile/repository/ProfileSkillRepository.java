package sinchonthon4.demo.domain.profile.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sinchonthon4.demo.domain.profile.entity.ProfileSkill;

public interface ProfileSkillRepository extends JpaRepository<ProfileSkill, Long> {

    List<ProfileSkill> findAllByProfile_IdOrderBySkill_IdAsc(Long profileId);
}
