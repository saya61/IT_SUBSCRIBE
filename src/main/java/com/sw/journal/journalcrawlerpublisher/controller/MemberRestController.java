package com.sw.journal.journalcrawlerpublisher.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.repository.CategoryRepository;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserFavoriteCategoryRepository;
import com.sw.journal.journalcrawlerpublisher.service.MemberService;
import com.sw.journal.journalcrawlerpublisher.service.ProfileImageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberRestController {
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileImageService profileImageService;
    private final UserFavoriteCategoryRepository userFavoriteCategoryRepository;
    private final ObjectMapper jacksonObjectMapper;

    // 회원가입 할 때 아이디, 이메일, 닉네임 중복 확인
    @PostMapping("/check-duplicate")
    public ResponseEntity<?> checkDuplicates(
            @RequestBody DuplicationCheckDTO request) throws IOException {
        // 응답 메시지 객체
        DuplicationCheckDTO response = new DuplicationCheckDTO();

        // 아이디 중복 확인
        if (request.getUsername() != null) {
            if (memberService.existsByUsername(request.getUsername())) {
                response.setUsername("이미 존재하는 아이디입니다.");
            } else {
                response.setUsername("사용할 수 있는 아이디입니다.");
            }
        }

        // 이메일 중복 확인
        if (request.getEmail() != null) {
            if (memberService.existsByEmail(request.getEmail())) {
                response.setEmail("이미 존재하는 이메일입니다.");
            } else {
                response.setEmail("사용할 수 있는 이메일입니다.");
            }
        }

        // 닉네임 중복 확인
        if (request.getNickname() != null) {
            if (memberService.existsByNickname(request.getNickname())) {
                response.setNickname("이미 존재하는 닉네임입니다.");
            } else {
                response.setNickname("사용할 수 있는 닉네임입니다.");
            }
        }

        return ResponseEntity.ok(response);
    }

    // 이메일 인증 코드 발송
    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(
            @RequestBody String emailRequest) throws IOException {
        // JSON 파싱
        JsonNode jsonNode = jacksonObjectMapper.readTree(emailRequest);
        String email = jsonNode.get("email").asText();

        // 인증번호 발송
        memberService.sendVerificationCode(email);
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    // 이메일 인증 코드 검증
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(
            // 사용자가 입력한 email, 인증번호를 body로 받음
            @RequestBody VerificationDTO request) throws IOException {

        // 인증 번호와 사용자 입력 코드 비교
        if (!memberService.verifyCode(request.getEmail(), request.getCode())) {
            return ResponseEntity.badRequest().body("인증코드가 일치하지 않습니다.");
        }

        // 인증 성공 후 DB에서 인증번호 삭제
        memberService.deleteCode(request.getEmail());
        return ResponseEntity.ok("인증이 완료되었습니다.");
    }

    // 비밀번호 설정 후 회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody MemberDTO request) throws IOException {

        // 비밀번호, 비밀번호 재입력값 일치 확인
        if(!request.getPassword().equals(request.getPassword2())) {
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
    public ResponseEntity<?> mypage() {
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
        return ResponseEntity.ok(new MypageDTO(currentMember));
    }

    // 닉네임 변경
    @PostMapping("/mypage/update-nickname")
    public ResponseEntity<String> updateNickname(@RequestBody String nicknameRequest) throws IOException{
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 권한 없음
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Member currentMember = member.get();

        // JSON 파싱
        JsonNode jsonNode = jacksonObjectMapper.readTree(nicknameRequest);
        String nickname = jsonNode.get("nickname").asText();

        // 닉네임 중복 확인
        if (memberService.existsByNickname(nickname)) {
            return ResponseEntity.badRequest().body("이미 존재하는 닉네임입니다.");
        }

        // 닉네임 변경
        if (!nicknameRequest.isEmpty()) {
            currentMember.setNickname(nickname);
        }
        memberRepository.save(currentMember);
        return ResponseEntity.ok("닉네임이 성공적으로 변경되었습니다.");
    }

    // 이메일 변경
    @PostMapping("/mypage/update-email")
    public ResponseEntity<String> updateEmail(@RequestBody String emailRequest) throws IOException {
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 권한 없음
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Member currentMember = member.get();

        // JSON 파싱
        JsonNode jsonNode = jacksonObjectMapper.readTree(emailRequest);
        String email = jsonNode.get("email").asText();

        // 이메일 중복 검사
        if (memberService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("이미 가입된 이메일입니다.");
        }

        // 이메일 변경
        if (!emailRequest.isEmpty()) {
            currentMember.setEmail(email);
        }
        memberRepository.save(currentMember);
        return ResponseEntity.ok("이메일이 성공적으로 변경되었습니다.");
    }


    // 비밀번호 재설정
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestParam("action") String action,
            @RequestBody ChangePwDTO request) {
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 권한 없음
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Member currentMember = member.get();

        // action을 통해 id 표시 또는 비밀번호 변경 중 선택
        switch (action) {
            // show_id일때는 body 값 {}로 보내야함
            case "show_id" :
                return ResponseEntity.ok(currentUsername);
            case "change_password" :
                if (!request.getNewPw().equals(request.getNewPwConfirm())) {
                    return ResponseEntity.badRequest().body("비밀번호가 맞지 않습니다.");
                }
                // 비밀번호 변경
                if (!request.getNewPw().isEmpty()) {
                    currentMember.setPassword(passwordEncoder.encode(request.getNewPw()));
                }
                memberRepository.save(currentMember);
                return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
            default:
                return ResponseEntity.badRequest().body("잘못된 요청입니다.");
        }
    }

    // 선호 카테고리 목록 출력
    @GetMapping("mypage/get-favorite-category")
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
        List<UserFavoriteCategory> userFavoriteCategories = memberService.findByMemberId(currentMember);

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
    @PostMapping("mypage/edit-favorite-category")
    @Transactional
    public ResponseEntity<String> editFavoriteCategory(
            // body에 [1, 2] 이런 식으로 리스트로 보냄
            @RequestBody List<Long> categoryIds) throws IOException {
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 권한 없음
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Member currentMember = member.get();

        // 기존 선호 카테고리 리스트 삭제
        userFavoriteCategoryRepository.deleteByMember(currentMember);

        // 새로운 선호 카테고리 리스트 반영
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

            memberService.saveAll(userFavoriteCategories);
        }
        return ResponseEntity.ok("선호 카테고리가 저장되었습니다.");
    }

    // 프로필 사진 변경
    @PostMapping("/mypage/update-profile-image")
    public ResponseEntity<?> updateProfileImage(
            // MultipartFile은 RequestBody로 전해주면 안된다고 합니다..
            @RequestParam("file") MultipartFile file) throws IOException {
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 권한 없음
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Member currentMember = member.get();

        // 프로필 사진 변경
        return ResponseEntity.ok(profileImageService.updateProfileImage(currentMember, file));
    }


}