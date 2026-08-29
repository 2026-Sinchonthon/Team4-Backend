package sinchonthon4.demo.domain.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sinchonthon4.demo.domain.user.entity.User;

@Entity
@Getter
@Table(
        name = "profiles",
        uniqueConstraints = @UniqueConstraint(name = "uk_profiles_user", columnNames = "user_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, length = 100)
    private String school;

    @Column(nullable = false, length = 100)
    private String major;

    @Column(nullable = false)
    private int grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Position position;

    @Column(length = 500)
    private String introduction;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Profile(User user, String nickname, String school, String major, int grade,
                    Position position, String introduction, String profileImageUrl,
                    String githubUrl, String linkedinUrl, String portfolioUrl) {
        this.user = user;
        this.nickname = nickname;
        this.school = school;
        this.major = major;
        this.grade = grade;
        this.position = position;
        this.introduction = introduction;
        this.profileImageUrl = profileImageUrl;
        this.githubUrl = githubUrl;
        this.linkedinUrl = linkedinUrl;
        this.portfolioUrl = portfolioUrl;
    }

    public static Profile create(User user, String nickname, String school, String major, int grade,
                                 Position position, String introduction, String profileImageUrl,
                                 String githubUrl, String linkedinUrl, String portfolioUrl) {
        return new Profile(user, nickname, school, major, grade, position, introduction,
                profileImageUrl, githubUrl, linkedinUrl, portfolioUrl);
    }

    public void updateDetails(String nickname, String school, String major, int grade,
                              Position position, String introduction, String profileImageUrl,
                              String githubUrl, String linkedinUrl, String portfolioUrl) {
        this.nickname = nickname;
        this.school = school;
        this.major = major;
        this.grade = grade;
        this.position = position;
        this.introduction = introduction;
        this.profileImageUrl = profileImageUrl;
        this.githubUrl = githubUrl;
        this.linkedinUrl = linkedinUrl;
        this.portfolioUrl = portfolioUrl;
    }
}
