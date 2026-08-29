package sinchonthon4.demo.domain.profile.service;

import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.profile.dto.ProfileOnboardingRequest;
import sinchonthon4.demo.domain.profile.dto.ProfileOnboardingResponse;
import sinchonthon4.demo.domain.profile.entity.Profile;
import sinchonthon4.demo.domain.profile.entity.ProfileSkill;
import sinchonthon4.demo.domain.profile.entity.Skill;
import sinchonthon4.demo.domain.profile.repository.ProfileRepository;
import sinchonthon4.demo.domain.profile.repository.ProfileSkillRepository;
import sinchonthon4.demo.domain.profile.repository.SkillRepository;
import sinchonthon4.demo.domain.user.entity.User;
import sinchonthon4.demo.domain.user.repository.UserRepository;
import sinchonthon4.demo.global.exception.BusinessException;
import sinchonthon4.demo.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class ProfileOnboardingService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final ProfileSkillRepository profileSkillRepository;

    @Transactional
    public ProfileOnboardingResponse onboard(Long currentUserId, ProfileOnboardingRequest request) {
        validateUniqueSkillIds(request.skillIds());

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (profileRepository.existsByUser_Id(currentUserId)) {
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }

        List<Skill> skills = findRequestedSkills(request.skillIds());
        Profile profile = Profile.create(
                user,
                request.nickname(),
                request.school(),
                request.major(),
                request.grade(),
                request.position(),
                request.introduction(),
                request.profileImageUrl(),
                request.githubUrl(),
                request.linkedinUrl(),
                request.portfolioUrl());

        try {
            profileRepository.saveAndFlush(profile);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }

        List<ProfileSkill> profileSkills = skills.stream()
                .map(skill -> ProfileSkill.create(profile, skill))
                .toList();
        profileSkillRepository.saveAll(profileSkills);

        return ProfileOnboardingResponse.of(profile, skills);
    }

    private void validateUniqueSkillIds(List<Long> skillIds) {
        if (new HashSet<>(skillIds).size() != skillIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private List<Skill> findRequestedSkills(List<Long> skillIds) {
        if (skillIds.isEmpty()) {
            return List.of();
        }

        List<Skill> skills = skillRepository.findAllByIdInOrderByIdAsc(skillIds);
        if (skills.size() != skillIds.size()) {
            throw new BusinessException(ErrorCode.SKILL_NOT_FOUND);
        }
        return skills;
    }
}
