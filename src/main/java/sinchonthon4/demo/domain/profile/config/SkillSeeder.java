package sinchonthon4.demo.domain.profile.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.profile.entity.Skill;
import sinchonthon4.demo.domain.profile.repository.SkillRepository;

@Component
@Order(2)
@RequiredArgsConstructor
public class SkillSeeder implements ApplicationRunner {

    private static final List<String> DEFAULT_SKILLS = List.of(
            "Java",
            "Spring",
            "Spring Boot",
            "React",
            "TypeScript",
            "JavaScript",
            "Python",
            "FastAPI",
            "MySQL",
            "Figma",
            "Git",
            "Docker",
            "AWS",
            "Kotlin",
            "Android",
            "Node.js",
            "Next.js",
            "C",
            "C++",
            "PyTorch",
            "TensorFlow"
    );

    private final SkillRepository skillRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String name : DEFAULT_SKILLS) {
            if (!skillRepository.existsByName(name)) {
                skillRepository.save(Skill.create(name));
            }
        }
    }
}
