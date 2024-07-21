//package com.sw.journal.journalcrawlerpublisher.repository;
//
//import com.sw.journal.journalcrawlerpublisher.constant.Role;
//import com.sw.journal.journalcrawlerpublisher.domain.Member;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.context.TestPropertySource;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.assertj.core.util.DateUtil.now;
//
//@SpringBootTest
////@TestPropertySource(locations = "classpath:application-test.properties")
//class MemberRepositoryTest {
//    @Autowired
//    private MemberRepository memberRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Test
//    @DisplayName("회원가입 테스트")
//    public void create() {
//        Member newMember = new Member();
//        newMember.setUsername("test");
//        newMember.setPassword(passwordEncoder.encode("test"));
//        newMember.setCreatedAt(LocalDateTime.now());
//        newMember.setNickname("test");
//        newMember.setEmail("sample.sam.ple");
//        newMember.setRole(Role.ADMIN);
//        memberRepository.save(newMember);
//
//        assert(memberRepository.existsByUsername(newMember.getUsername()));
//    }
//
//    @Test
//    @DisplayName("아이디 필드 기준 유저 검색")
//    public void findByUsername() {
//        create();
//        // 존재하는 유저 검색
//        Optional<Member> foundUser = memberRepository.findByUsername("test");
//        assert foundUser.isPresent();
//        // 존재하지 않는 유저 검색
//        foundUser = memberRepository.findByUsername("test2");
//        assert foundUser.isEmpty();
//    }
//
//    @Test
//    @DisplayName("유저 이메일 중복 검사 테스트")
//    public void duplicateEmail() {
//        create();
//        boolean exists = memberRepository.existsByEmail("sample.sam.ple");
//        assert(exists);
//
//        exists = memberRepository.existsByNickname("sample1.sam.ple");
//        assert(!exists);
//    }
//
//    @Test
//    @DisplayName("유저 아이디 중복 검사 테스트")
//    public void duplicateUsername() {
//        create();
//        boolean exists = memberRepository.existsByUsername("test");
//        assert(exists);
//
//        exists = memberRepository.existsByNickname("test1");
//        assert(!exists);
//    }
//
//    @Test
//    @DisplayName("유저 닉네임 중복 검사 테스트")
//    public void duplicateNickname() {
//        create();
//        boolean exists = memberRepository.existsByNickname("test");
//        assert(exists);
//
//        exists = memberRepository.existsByNickname("test1");
//        assert(!exists);
//    }
//
//    // 이메일, 아이디, 닉네임 중복 검사 한꺼번에
//    @Test
//    public void validateDuplicateUser() {
//        create();
//        Member member = new Member();
//        member.setUsername("test");
//        member.setPassword(passwordEncoder.encode("test"));
//        member.setCreatedAt(LocalDateTime.now());
//        member.setNickname("test");
//        member.setEmail("sample.sam.ple");
//        member.setRole(Role.ADMIN);
//        assert(memberRepository.existsByEmail(member.getEmail()));
//        assert(memberRepository.existsByUsername(member.getUsername()));
//        assert(memberRepository.existsByNickname(member.getNickname()));
//    }
//}