package sinchonthon4.demo.domain.profile.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sinchonthon4.demo.domain.profile.entity.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    boolean existsByName(String name);

    Optional<Skill> findByName(String name);

    List<Skill> findAllByIdInOrderByIdAsc(Collection<Long> ids);

    List<Skill> findAllByOrderByIdAsc();
}
