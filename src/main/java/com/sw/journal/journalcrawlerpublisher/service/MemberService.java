package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.constant.Role;
import com.sw.journal.journalcrawlerpublisher.domain.Ban;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
//import com.sw.journal.journalcrawlerpublisher.domain.SpringUser;
import com.sw.journal.journalcrawlerpublisher.domain.UserFavoriteCategory;
import com.sw.journal.journalcrawlerpublisher.domain.VerificationCode;
import com.sw.journal.journalcrawlerpublisher.repository.BanRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Service
@Transactional  // All or nothing -> 실패 시 발생하는 예외 처리를 위해 사용
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final UserFavoriteCategoryRepository userFavoriteCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final BanRepository banRepository;

//    // 로그인 기능 구현
//    @Override
//    public UserDetails loadUserByUsername(String username)
//            throws UsernameNotFoundException {
//        Optional<Member> registeredUser = memberRepository.findByUsername(username);
//        if (registeredUser.isEmpty()) {
//            throw new UsernameNotFoundException(username);
//        }
//        // 인증에 사용하기 위해 준비된 UserDetails 구현체
//        return SpringUser.getSpringUserDetails(registeredUser.get());
//    }

    // 유저 생성 메서드
    public void create(String username, String nickname, String email, String password) {
        Member newMember = new Member(); // Member 객체 생성
        newMember.setUsername(username); // 유저 id 설정
        newMember.setNickname(nickname); // 유저 닉네임 설정
        newMember.setEmail(email); // 유저 이메일 설정
        newMember.setPassword(passwordEncoder.encode(password)); // 유저 비밀번호(복호화) 설정
        newMember.setCreatedAt(LocalDateTime.now()); // 유저 생성 날짜 설정
        newMember.setRole(Role.USER); // 유저 권한 설정

        // 중복 유저 체크
        validateDuplicateUser(newMember);
        // 생성한 Member 객체를 DB에 저장
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

    // 유저 id로 유저 선호 카테고리 검색
    public List<UserFavoriteCategory> findByMemberId(Member member) {
        return userFavoriteCategoryRepository.findByMember(member);
    }

    // 유저 선호 카테고리 저장
    public void saveAll(List<UserFavoriteCategory> userFavoriteCategories) {
        userFavoriteCategoryRepository.saveAll(userFavoriteCategories);
    }

    // 유저 id로 유저 검색
    public Optional<Member> findByUsername(String currentUsername) {
        return memberRepository.findByUsername(currentUsername);
    }

    // 계정 정지 상태와 종료일 확인
    public Optional<LocalDate> getBanEndDateByMemberId(Long memberId) {
        return banRepository.findTopByMemberIdAndActiveTrueOrderByBanEndDateDesc(memberId)
                .map(Ban::getBanEndDate);
    }

}
