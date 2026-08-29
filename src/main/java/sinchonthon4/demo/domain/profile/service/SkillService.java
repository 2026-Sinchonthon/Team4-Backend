package sinchonthon4.demo.domain.profile.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.profile.dto.SkillResponse;
import sinchonthon4.demo.domain.profile.repository.SkillRepository;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    @Transactional(readOnly = true)
    public List<SkillResponse> getSkills() {
        return skillRepository.findAllByOrderByIdAsc().stream()
                .map(SkillResponse::from)
                .toList();
    }
}
