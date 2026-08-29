package sinchonthon4.demo.domain.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sinchonthon4.demo.domain.group.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
