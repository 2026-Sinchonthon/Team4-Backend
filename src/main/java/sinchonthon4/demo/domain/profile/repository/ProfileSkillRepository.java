package sinchonthon4.demo.domain.profile.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sinchonthon4.demo.domain.profile.entity.ProfileSkill;

public interface ProfileSkillRepository extends JpaRepository<ProfileSkill, Long> {

    @Query("""
            select ps from ProfileSkill ps
            join fetch ps.skill
            where ps.profile.id = :profileId
            order by ps.skill.id asc
            """)
    List<ProfileSkill> findAllByProfile_IdOrderBySkill_IdAsc(@Param("profileId") Long profileId);

    @Modifying
    @Query("delete from ProfileSkill ps where ps.profile.id = :profileId")
    int deleteAllByProfileId(@Param("profileId") Long profileId);
}
