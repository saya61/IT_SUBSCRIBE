package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.repository.CategoryRepository;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserFavoriteCategoryRepository;
import com.sw.journal.journalcrawlerpublisher.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberRestController {
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final UserFavoriteCategoryRepository userFavoriteCategoryRepository;
    private final CategoryRepository categoryRepository;

    // 아이디  중복 확인
    @PostMapping("/checkId")
    public ResponseEntity<String> validateDuplicateId(
            @RequestParam("username")String username) throws IOException {

        if(memberService.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다.");
        }

        return ResponseEntity.ok("사용할 수 있는 아이디입니다.");
    }

    // 닉네임  중복 확인
    @PostMapping("/checkNickname")
    public ResponseEntity<String> validateDuplicateNickname(
            @RequestParam("nickname")String nickname) throws IOException {

            if(memberService.existsByNickname(nickname)) {
            return ResponseEntity.badRequest().body("이미 존재하는 닉네임입니다.");
            }

        return ResponseEntity.ok("사용할 수 있는 닉네임입니다.");
    }

    // 이메일 인증 코드 발송
    @PostMapping("/sendCode")
    public ResponseEntity<VerificationResponse> sendCode(
            @RequestParam("email")String email) throws IOException {
        // 응답 메세지
        String res;

        // 이메일 중복 확인
        if(memberService.existsByEmail(email)) {
            res = "이미 가입한 이메일입니다.";
            return ResponseEntity.badRequest().body(
                new VerificationResponse(null,null, res)
            );
        }

        // 인증번호 발송
        String code = memberService.sendVerificationCode(email);
        res = "인증번호가 발송되었습니다.";
        return ResponseEntity.ok(
            // email, code 를 포함하는 json 변환 가능한 객체 타입을 body 로 반환
            new VerificationResponse(email, code, res)
        );
    }

    // 이메일 인증 코드 검증
    @PostMapping("/verifyCode")
    // @RequestParam -> URL 파라미터로 code 값을 받음
    // @RequestBody -> JSON 형식으로 BODY에서 받음
    public ResponseEntity<String> verifyCode(
            @RequestParam("code") String code,
            @RequestBody VerificationRequest request) throws IOException {

        // 인증 번호와 사용자 입력 코드 비교
        if (!memberService.verifyCode(request.getEmail(),code)) {
            return ResponseEntity.badRequest().body("인증코드가 일치하지 않습니다.");
        }

        // 인증 성공 후 코드 삭제
        memberService.deleteCode(request.getEmail());
        return ResponseEntity.ok("인증이 완료되었습니다.");
    }

    // 비밀번호 중복 확인
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestParam("password") String password,
            @RequestParam("password2") String password2,
            @RequestBody VerificationRequest request) throws IOException {

        // 비밀번호, 비밀번호 재입력값 일치 확인
        if(!password.equals(password2)) {
            return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
        }

        // 신규 회원 데이터 DB에 저장
        memberService.create(
                request.getUsername(), request.getNickname(), request.getEmail(), request.getPassword()
        );
        return ResponseEntity.ok("회원 가입되었습니다.");
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    // members/login을 통해 로그인된 상태여야지 GET 요청 테스트가 성공했음
    // 마이페이지 조회
    @GetMapping("/mypage")
    public ResponseEntity<MemberDTO> mypage() {
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 권한 없음
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // 현재 사용자 정보 반환
        Member currentMember = member.get();
        return ResponseEntity.ok(new MemberDTO(currentMember));
    }

    // 닉네임 변경
    @PostMapping("/mypage/updateNickname")
    public ResponseEntity<String> updateNickname(@RequestParam("nickname") String nickname) throws IOException{
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 권한 없음
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Member currentMember = member.get();

        // 닉네임 중복 확인
        if (memberService.existsByNickname(nickname)) {
            return ResponseEntity.badRequest().body("이미 존재하는 닉네임입니다.");
        }

        // 닉네임 변경
        if (!nickname.isEmpty()) {
            currentMember.setNickname(nickname);
        }
        memberRepository.save(currentMember);
        return ResponseEntity.ok("닉네임이 성공적으로 변경되었습니다.");
    }

    // 이메일 변경
    @PostMapping("/mypage/updateEmail")
    public ResponseEntity<String> updateEmail(@RequestParam("email") String email) throws IOException {
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 권한 없음
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Member currentMember = member.get();

        // 이메일 중복 검사
        if (memberService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("이미 가입된 이메일입니다.");
        }

        // 3. 이메일 변경
        if (!email.isEmpty()) {
            currentMember.setEmail(email);
        }
        memberRepository.save(currentMember);
        return ResponseEntity.ok("이메일이 성공적으로 변경되었습니다.");
    }

    // 비밀번호 변경 (currentPw는 현재 사용자 비번, newPw는 변경할 비번, confirmPw는 변경할 비번 재확인)
    @PostMapping("/mypage/updatePassword")
    public ResponseEntity<String> updatePassword(
            @RequestParam("currentPw") String currentPw,
            @RequestParam("newPw") String newPw,
            @RequestParam("confirmPw") String confirmPw) throws IOException {
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 권한 없음
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Member currentMember = member.get();
        String currentPassword = currentMember.getPassword();

        if (!newPw.equals(confirmPw)) {
            return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
        }
        if (!passwordEncoder.matches(currentPw, currentPassword)) {
            return ResponseEntity.badRequest().body("잘못된 비밀번호입니다.");
        }

        // 비밀번호 변경
        if (!newPw.isEmpty()) {
            currentMember.setPassword(passwordEncoder.encode(newPw));
        }
        memberRepository.save(currentMember);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

    // 선호 카테고리 목록 출력
    @GetMapping("mypage/getFavoriteCategory")
    public ResponseEntity<String> getFavoriteCategory() throws IOException {
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 권한 없음
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // 현재 사용자 id로 선호 카테고리 검색
        Member currentMember = member.get();
        List<UserFavoriteCategory> userFavoriteCategories = memberService.findByMember(currentMember);

        // 현재 사용자의 선호 카테고리 목록을 카테고리 이름으로 매핑하여 반환
        List<String> favoriteCategoryNames = userFavoriteCategories.stream()
                // 각 UserFavoriteCategory 객체에서 Category 객체를 추출
                .map(UserFavoriteCategory::getCategory)
                // 각 Category 객체에서 카테고리 이름 추출
                .map(Category::getName)
                .toList();

        return ResponseEntity.ok(favoriteCategoryNames.toString());
    }

    // 선호 카테고리 편집
    @PostMapping("mypage/editFavoriteCategory")
    public ResponseEntity<String> editFavoriteCategory(
            @RequestParam("categoryIds")List<Long> categoryIds) throws IOException {
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 권한 없음
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // 선호 카테고리 리스트를 기반으로 Stream 생성
        // categoryIds는 사용자가 선택한 카테고리 ID 목록
        if (!categoryIds.isEmpty()) {
            List<UserFavoriteCategory> userFavoriteCategories = categoryIds.stream()
                    // 각 categoryId를 입력으로 받아서 UserFavoriteCategory 객체를 생성하는 람다 표현식
                    .map(categoryId -> {
                        // 각 UserFavoriteCategory 객체에는 현재 member, 선택한 category에 대응하는 Category 객체가 설정됨
                        UserFavoriteCategory userFavoriteCategory = new UserFavoriteCategory();
                        userFavoriteCategory.setMember(member.get());
                        userFavoriteCategory.setCategory(categoryRepository.findById(categoryId).orElseThrow());
                        userFavoriteCategory.setId(new UserFavoriteCategoryId(member.get().getId(), categoryId));
                        return userFavoriteCategory;
                    })
                    .toList();

            userFavoriteCategoryRepository.saveAll(userFavoriteCategories);
        }
        return ResponseEntity.ok("선호 카테고리가 저장되었습니다.");
    }
}