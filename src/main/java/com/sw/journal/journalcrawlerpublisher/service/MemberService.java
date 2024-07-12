package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.constant.Role;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.SpringUser;
import com.sw.journal.journalcrawlerpublisher.domain.UserFavoriteCategory;
import com.sw.journal.journalcrawlerpublisher.domain.VerificationCode;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserFavoriteCategoryRepository;
import com.sw.journal.journalcrawlerpublisher.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;


@Service
@Transactional  // All or nothing -> 실패 시 발생하는 예외 처리를 위해 사용
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final UserFavoriteCategoryRepository userFavoriteCategoryRepository;
    private final PasswordEncoder passwordEncoder;

    // 로그인 기능 구현
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        Optional<Member> registeredUser = memberRepository.findByUsername(username);
        if (registeredUser.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        // 인증에 사용하기 위해 준비된 UserDetails 구현체
        return SpringUser.getSpringUserDetails(registeredUser.get());
    }

    // CRUD 기능 구현
    // 유저 생성 메서드
    public void create(String username, String nickname, String email, String password) {
        Member newMember = new Member();
        newMember.setUsername(username);
        newMember.setNickname(nickname);
        newMember.setEmail(email);
        newMember.setPassword(passwordEncoder.encode(password));
        newMember.setCreatedAt(LocalDateTime.now());
        newMember.setRole(Role.USER);

        // 중복 유저 체크
        validateDuplicateUser(newMember);
        memberRepository.save(newMember);
    }

    // 중복 유저 검사 메서드
    public void validateDuplicateUser(Member member) {
        if (existsByUsername(member.getUsername())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }
        if (existsByNickname(member.getNickname())) {
            throw new IllegalStateException("이미 존재하는 닉네임입니다.");
        }
        if (existsByEmail(member.getEmail())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }
    }

    // 중복 아이디 검사 메서드
    public boolean existsByUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    // 중복 닉네임 검사 메서드
    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    // 중복 이메일 검사 메서드
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 난수 생성 메서드
    public String sendVerificationCode(String email) {
//        String code = String.format("%06d", new Random().nextInt(999999));
        // 일단 난수 랜덤 생성하지 않고 123456으로 통일
        String code = "123456";
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setCreatedAt(LocalDateTime.now());
        verificationCodeRepository.save(verificationCode);

        return code;
    }

    // 난수 검증 메서드
    public boolean verifyCode(String email, String code) {
        Optional<VerificationCode> optionalVerificationCode = verificationCodeRepository.findByEmail(email);
        if (optionalVerificationCode.isPresent()) {
            VerificationCode verificationCode = optionalVerificationCode.get();
            return verificationCode.getCode().equals(code);
        }
        return false;
    }

    // 난수 삭제 메서드
    public void deleteCode(String email) {
        verificationCodeRepository.deleteByEmail(email);
    }

    // 유저 id로 유저 선호 카테고리 검색
    public List<UserFavoriteCategory> findByMemberId(Member member) {
        return userFavoriteCategoryRepository.findByMember(member);
    }

    // 유저 선호 카테고리 저장
    public void saveAll(List<UserFavoriteCategory> userFavoriteCategories) {
        userFavoriteCategoryRepository.saveAll(userFavoriteCategories);
    }
}
