package sinchonthon4.demo.domain.profile.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "profile_skills",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_profile_skills_profile_skill",
                columnNames = {"profile_id", "skill_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    private ProfileSkill(Profile profile, Skill skill) {
        this.profile = profile;
        this.skill = skill;
    }

    public static ProfileSkill create(Profile profile, Skill skill) {
        return new ProfileSkill(profile, skill);
    }
}
