package sinchonthon4.demo.domain.profile.service;

import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.profile.dto.MyProfileResponse;
import sinchonthon4.demo.domain.profile.dto.ProfileUpdateRequest;
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
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final ProfileSkillRepository profileSkillRepository;

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long currentUserId) {
        User user = getUser(currentUserId);
        Profile profile = getProfile(currentUserId);
        return MyProfileResponse.of(user, profile, getSkills(profile.getId()));
    }

    @Transactional
    public MyProfileResponse updateMyProfile(Long currentUserId, ProfileUpdateRequest request) {
        User user = getUser(currentUserId);
        Profile profile = getProfile(currentUserId);
        validateUniqueSkillIds(request.skillIds());
        List<Skill> skills = findRequestedSkills(request.skillIds());

        profile.updateDetails(
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

        profileSkillRepository.deleteAllByProfileId(profile.getId());
        profileSkillRepository.saveAll(skills.stream()
                .map(skill -> ProfileSkill.create(profile, skill))
                .toList());

        return MyProfileResponse.of(user, profile, skills);
    }

    private User getUser(Long currentUserId) {
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Profile getProfile(Long currentUserId) {
        return profileRepository.findByUser_Id(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
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

    private List<Skill> getSkills(Long profileId) {
        return profileSkillRepository.findAllByProfile_IdOrderBySkill_IdAsc(profileId).stream()
                .map(ProfileSkill::getSkill)
                .toList();
    }
}
