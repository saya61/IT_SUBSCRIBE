package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.constant.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import com.sw.journal.journalcrawlerpublisher.domain.User;

import java.util.Optional;

import static org.assertj.core.util.DateUtil.now;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 테스트")
    public void create() {
        User newUser = new User();
        newUser.setUserId("test");
        newUser.setUserPw(passwordEncoder.encode("test"));
        newUser.setUserCreatedAt(now());
        newUser.setUserNickname("test");
        newUser.setUserEmail("sample.sam.ple");
        newUser.setUserRole(UserRole.ADMIN);
        userRepository.save(newUser);

        assert(userRepository.existsByUserId(newUser.getUserId()));
    }

    @Test
    @DisplayName("아이디 필드 기준 유저 검색")
    public void findByUserId() {
        create();
        // 존재하는 유저 검색
        Optional<User> foundUser = userRepository.findByUserId("test");
        assert foundUser.isPresent();
        // 존재하지 않는 유저 검색
        foundUser = userRepository.findByUserId("test2");
        assert foundUser.isEmpty();
    }

    @Test
    @DisplayName("유저 이메일 중복 검사 테스트")
    public void duplicateUserEmail() {
        create();
        boolean exists = userRepository.existsByUserEmail("sample.sam.ple");
        assert(exists);

        exists = userRepository.existsByUserNickname("sample1.sam.ple");
        assert(!exists);
    }

    @Test
    @DisplayName("유저 아이디 중복 검사 테스트")
    public void duplicateUserId() {
        create();
        boolean exists = userRepository.existsByUserId("test");
        assert(exists);

        exists = userRepository.existsByUserNickname("test1");
        assert(!exists);
    }

    @Test
    @DisplayName("유저 닉네임 중복 검사 테스트")
    public void duplicateUserNickname() {
        create();
        boolean exists = userRepository.existsByUserNickname("test");
        assert(exists);

        exists = userRepository.existsByUserNickname("test1");
        assert(!exists);
    }

    // 이메일, 아이디, 닉네임 중복 검사 한꺼번에
    @Test
    public void validateDuplicateUser() {
        create();
        User user = new User();
        user.setUserId("test");
        user.setUserPw(passwordEncoder.encode("test"));
        user.setUserCreatedAt(now());
        user.setUserNickname("test");
        user.setUserEmail("sample.sam.ple");
        user.setUserRole(UserRole.ADMIN);
        assert(userRepository.existsByUserEmail(user.getUserEmail()));
        assert(userRepository.existsByUserId(user.getUserId()));
        assert(userRepository.existsByUserNickname(user.getUserNickname()));
    }
}