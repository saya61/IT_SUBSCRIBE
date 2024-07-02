package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.constant.UserRole;
import com.sw.journal.journalcrawlerpublisher.domain.User;
import com.sw.journal.journalcrawlerpublisher.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.util.DateUtil.now;

@Service
@Transactional  // All or nothing -> 실패 시 발생하는 예외 처리를 위해 사용
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 유저 생성 메서드
    public void create(String userId, String userNickname, String userEmail, String userPw) {
        User newUser = new User();
        newUser.setUserId(userId);
        newUser.setUserNickname(userNickname);
        newUser.setUserEmail(userEmail);
        newUser.setUserPw(passwordEncoder.encode(userPw));
        newUser.setUserCreatedAt(now());
        newUser.setUserRole(UserRole.USER);

        // 중복 유저 체크
        validateDuplicateUser(newUser);
        User savedUser = userRepository.save(newUser);
    }

    // 중복 유저 검사 메서드
    public void validateDuplicateUser(User user) {
        if (existsByUserId(user.getUserId())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }
        if (existsByUserNickname(user.getUserNickname())) {
            throw new IllegalStateException("이미 존재하는 닉네임입니다.");
        }
        if (existsByUserEmail(user.getUserEmail())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }
    }

    // 중복 아이디 검사 메서드
    public boolean existsByUserId(String userId) {
        return userRepository.existsByUserId(userId);
    }

    // 중복 닉네임 검사 메서드
    public boolean existsByUserNickname(String userNickname) {
        return userRepository.existsByUserNickname(userNickname);
    }

    // 중복 이메일 검사 메서드
    public boolean existsByUserEmail(String userEmail) {
        return userRepository.existsByUserEmail(userEmail);
    }
}
