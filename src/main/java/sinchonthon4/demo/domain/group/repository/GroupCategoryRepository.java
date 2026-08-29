package sinchonthon4.demo.domain.group.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sinchonthon4.demo.domain.group.entity.GroupCategory;

public interface GroupCategoryRepository extends JpaRepository<GroupCategory, Long> {

    boolean existsByName(String name);

    Optional<GroupCategory> findByName(String name);
}
