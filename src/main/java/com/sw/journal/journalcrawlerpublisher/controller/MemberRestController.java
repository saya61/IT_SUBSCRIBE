package com.sw.journal.journalcrawlerpublisher.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.repository.CategoryRepository;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserFavoriteCategoryRepository;
import com.sw.journal.journalcrawlerpublisher.service.MemberService;
import com.sw.journal.journalcrawlerpublisher.service.ProfileImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberRestController {
    private final MemberService memberService;
    private final ProfileImageService profileImageService;

    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final UserFavoriteCategoryRepository userFavoriteCategoryRepository;

    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper jacksonObjectMapper;

    // 프로필 이미지 업로드 경로
    @Value("${upload.path}")
    private String uploadDir;

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

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO request, HttpServletRequest httpRequest) {
        Optional<Member> optionalMember = memberRepository.findByUsername(request.getId());

        if (optionalMember.isEmpty()) {
            return ResponseEntity.badRequest().body("회원이 존재하지 않습니다");
        }
        Member member = optionalMember.get();

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
        }

        // 세션 저장 및 세션 키 헤더 (setCookie) 응답
        UserDetails userDetails = User.withUsername(member.getUsername())
                .password(member.getPassword())
                .roles("USER").build();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        return ResponseEntity.ok("로그인에 성공했습니다.");
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
            // action을 통해 user ID 표시 또는 비밀번호 변경 중 선택
            @RequestParam("action") String action,
            // user ID 표시하기 위해 RequestParam으로 user ID를 전달 받음
            @RequestParam("id") String id,
            // 사용자가 입력한 new password, Confirm password 값 비교하기 위한 DTO
            @RequestBody ChangePwDTO request) {
        // RequestParam을 통해 전달 받은 user ID로 사용자 검색
        Optional<Member> member = memberRepository.findByUsername(id);
        // 전달 받은 user ID의 사용자가 존재하지 않을때
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자를 찾을 수 없습니다.");
        }

        switch (action) {
            // user ID 표시하는 부분
            // show_id일때는 body 값 {}로 보내야함
            case "show_id" :
                return ResponseEntity.ok(id);
            // new password, Confirm password 부분
            case "change_password" :
                // new password, Confirm password 일치하지 않을 때
                if (!request.getNewPw().equals(request.getNewPwConfirm())) {
                    return ResponseEntity.badRequest().body("비밀번호가 맞지 않습니다.");
                // 일치할시 비밀번호가 성공적으로 변경됨
                } else {
                    Member Targetmember = member.get();
                    Targetmember.setPassword(passwordEncoder.encode(request.getNewPw()));
                    memberRepository.save(Targetmember);
                    return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
                }
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


    // React에서 이미지를 표시하기 위해 이미지를 다운로드할 수 있는 엔드포인트
    // 파일을 읽어 HTTP 응답으로 반환
    @GetMapping("/mypage/get-profile-image")
    public ResponseEntity<Resource> getProfileImage(@RequestParam("filename") String filename) {
        try {
            // 파일 업로드 경로
            File uploadDirFile = new File(uploadDir);

            // 파일의 절대 경로 생성
            Path filePath = Paths.get(uploadDirFile.getAbsolutePath()).resolve(filename).normalize();
            // 파일 경로를 URI로 변환하여 UrlResource 객체 생성
            Resource resource = new UrlResource(filePath.toUri());

            // 리소스가 존재하는지 확인
            if (resource.exists()) {
                // 기본 Content-Type 설정
                String contentType = "application/octet-stream";
                // 파일 확장자에 따라 적절한 Content-Type 설정
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.endsWith(".png")) {
                    contentType = "image/png";
                }

                // 리소스가 존재하면 HTTP 응답을 생성하여 반환
                return ResponseEntity.ok() // 200 OK 상태 코드 설정
                        // Content-Disposition 헤더를 설정하여 파일이 첨부 파일로 다운로드되도록 함
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        // 적절한 Content-Type 헤더를 설정
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        // 리소스를 응답 본문에 포함
                        .body(resource);
            } else { // 리소스가 존재하지 않으면 404 Not Found 상태 코드 반환
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (MalformedURLException ex) { // 파일 경로가 잘못되었을 경우 500 Internal Server Error 상태 코드 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}