package com.example.portfolio.service;

import com.example.portfolio.dto.Requests.UserSkillRequest;
import com.example.portfolio.dto.Responses.SkillResponse;
import com.example.portfolio.dto.Responses.UserSkillResponse;
import com.example.portfolio.entity.Skill;
import com.example.portfolio.entity.User;
import com.example.portfolio.entity.UserSkill;
import com.example.portfolio.exception.AppException;
import com.example.portfolio.repository.SkillRepository;
import com.example.portfolio.repository.UserSkillRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillService {
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;

    public SkillService(SkillRepository skillRepository, UserSkillRepository userSkillRepository) {
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
    }

    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream().map(SkillResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<UserSkillResponse> getUserSkills(User user) {
        return userSkillRepository.findByUser(user).stream().map(UserSkillResponse::from).toList();
    }

    @Transactional
    public UserSkillResponse addUserSkill(User user, UserSkillRequest request) {
        Skill skill = skillRepository.findById(request.skillId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "기술 스택을 찾을 수 없습니다."));
        UserSkill userSkill = userSkillRepository.findByUserAndSkill(user, skill).orElseGet(UserSkill::new);
        userSkill.setUser(user);
        userSkill.setSkill(skill);
        userSkill.setLevel(request.level());
        return UserSkillResponse.from(userSkillRepository.save(userSkill));
    }

    @Transactional
    public void deleteUserSkill(User user, Long skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "기술 스택을 찾을 수 없습니다."));
        userSkillRepository.deleteByUserAndSkill(user, skill);
    }
}
