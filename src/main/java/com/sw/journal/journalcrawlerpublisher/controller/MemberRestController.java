package com.sw.journal.journalcrawlerpublisher.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.dto.*;
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

    // Repository 레이어를 사용해 직접 DB 접근. 서비스 레이어로의 리팩토링이 권장됨
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final UserFavoriteCategoryRepository userFavoriteCategoryRepository;

    // 비밀번호 암호화를 위한 PasswordEncoder 인스턴스
    private final PasswordEncoder passwordEncoder;
    // JSON 데이터를 파싱하기 위한 ObjectMapper 인스턴스
    private final ObjectMapper jacksonObjectMapper;

    // 프로필 이미지 업로드 경로를 application.properties 에서 읽어옴
    @Value("${upload.path}")
    private String uploadDir;

    // 회원가입 할 때 아이디, 이메일, 닉네임 중복 확인
    @PostMapping("/check-duplicate")
    public ResponseEntity<?> checkDuplicates(
            @RequestBody DuplicationCheckDTO request) throws IOException {
        // 응답 메시지를 담을 DuplicationCheckDTO 객체 생성
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
        // 중복 확인 결과를 클라이언트에 반환
        return ResponseEntity.ok(response);
    }

    // 비밀번호 설정 후 회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody MemberDTO request) throws IOException {

        // 입력한 비밀번호와 비밀번호 재입력값 일치 확인
        if(!request.getPassword().equals(request.getPassword2())) {
            return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
        }

        // 일치하면 유저 생성 후 DB에 저장
        memberService.create(
                request.getUsername(), request.getNickname(), request.getEmail(), request.getPassword()
        );
        return ResponseEntity.ok("회원 가입되었습니다.");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO request, HttpServletRequest httpRequest) {
        // 사용자 id로 Member 객체 조회
        Optional<Member> optionalMember = memberRepository.findByUsername(request.getId());
        // 사용자가 존재하지 않은 경우 오류 반환
        if (optionalMember.isEmpty()) {
            return ResponseEntity.badRequest().body("회원이 존재하지 않습니다");
        }
        // 사용자가 존재할 경우 조회된 Member 객체에서 정보 추출
        Member member = optionalMember.get();

        // 비밀번호가 일치하지 않으면 오류 반환
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
        }

        // 인증 정보 생성 및 SecurityContext 설정
        UserDetails userDetails = User.withUsername(member.getUsername()) // 인증된 사용자의 사용자 id를 기반으로 UserDetails 객체 생성
                .password(member.getPassword()) // 사용자 비밀번호 설정(비밀번호는 이미 암호화된 상태)
                .roles("USER").build(); // 사용자에게 "USER" 권한 부여
        SecurityContext securityContext = SecurityContextHolder.getContext(); // 현재 스레드와 관련된 SecurityContext 객체를 가져옴
        securityContext.setAuthentication( // SecurityContext에 인증 정보(Authentication) 설정
                new UsernamePasswordAuthenticationToken( // UsernamePasswordAuthenticationToken 객체 생성
                        userDetails, // 인증된 사용자 정보(UserDetails)
                        null, // 인증 정보가 없다면 비밀번호(null)
                        userDetails.getAuthorities() // 사용자의 권한 정보
                )
        );

        // 세션 생성 및 SecurityContext 저장
        HttpSession session = httpRequest.getSession(true); // 현재 요청과 관련된 HTTP 세션을 가져옴 (세션이 없다면 새로 생성)
        session.setAttribute( // 세션에 속성 추가
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, // SecurityContext 를 저장하는 데 사용되는 키
                securityContext // 위에서 설정한 SecurityContext 객체를 세션에 저장
        );

        // 사용자 정보와 성공 메시지 반환
        Map<String, Object> userInfo = new HashMap<>(); // 클라이언트에게 반환할 사용자 정보를 저장할 맵 객체 생성
        userInfo.put("message", "로그인에 성공했습니다."); // 로그인 성공 메시지 추가
        userInfo.put("nickname", member.getNickname()); // 사용자 닉네임 추가
        userInfo.put("email", member.getEmail()); // 사용자 이메일 추가
        userInfo.put("avatarUrl", member.getProfileImage() != null // 사용자 프로필 이미지가 존재하는지 확인
                ? member.getProfileImage().getFileUrl() // 프로필 이미지가 있으면 이미지 URL 추가
                : null); // 프로필 이미지가 없으면 null 값 설정

        // 사용자 정보를 포함한 HTTP 200 OK 응답을 클라이언트에게 반환
        return ResponseEntity.ok(userInfo);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // 현재 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // SecurityContext 를 클리어하여 사용자 인증 정보 삭제
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("로그아웃에 성공했습니다.");
    }

    // 마이페이지 조회
    @GetMapping("/mypage")
    public ResponseEntity<?> mypage() {
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        // 조회된 Member 객체에서 정보 추출
        Member currentMember = member.get();

        // 현재 로그인한 사용자 정보를 MypageDTO로 변환하여 반환
        return ResponseEntity.ok(new MypageDTO(currentMember));
    }

    // 닉네임 변경
    @PostMapping("/mypage/update-nickname")
    public ResponseEntity<String> updateNickname(@RequestBody String nicknameRequest) throws IOException{
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        // 조회된 Member 객체에서 정보 추출
        Member currentMember = member.get();

        // JSON 데이터를 파싱하여 닉네임 추출
        JsonNode jsonNode = jacksonObjectMapper.readTree(nicknameRequest);
        String nickname = jsonNode.get("nickname").asText();

        // 닉네임 중복 확인
        if (memberService.existsByNickname(nickname)) {
            return ResponseEntity.badRequest().body("이미 존재하는 닉네임입니다.");
        }

        // 닉네임이 유효하면 변경 후 DB에 저장
        if (!nicknameRequest.isEmpty()) {
            currentMember.setNickname(nickname);
        }
        memberRepository.save(currentMember);
        return ResponseEntity.ok("닉네임이 성공적으로 변경되었습니다.");
    }

    // 이메일 변경
    @PostMapping("/mypage/update-email")
    public ResponseEntity<String> updateEmail(@RequestBody String emailRequest) throws IOException {
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // 조회된 Member 객체에서 정보 추출
        Member currentMember = member.get();

        // JSON 데이터를 파싱하여 이메일 추출
        JsonNode jsonNode = jacksonObjectMapper.readTree(emailRequest);
        String email = jsonNode.get("email").asText();

        // 이메일 중복 검사
        if (memberService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("이미 가입된 이메일입니다.");
        }

        // 이메일이 유효하면 변경 후 DB에 저장
        if (!emailRequest.isEmpty()) {
            currentMember.setEmail(email);
        }
        memberRepository.save(currentMember);
        return ResponseEntity.ok("이메일이 성공적으로 변경되었습니다.");
    }


    // 비밀번호 재설정
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            // action을 통해 사용자 ID 표시 또는 비밀번호 변경 중 선택
            @RequestParam("action") String action,
            // 사용자 ID 표시하기 위해 RequestParam으로 사용자 ID를 전달 받음
            @RequestParam("id") String id,
            // 사용자가 입력한 new password, Confirm password 값 비교하기 위한 DTO
            @RequestBody ChangePwDTO request) {
        // RequestParam을 통해 전달 받은 사용자 ID로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(id);
        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자를 찾을 수 없습니다.");
        }

        switch (action) {
            // 사용자 ID 표시
            // show_id일때는 body 값 {}로 보내야함
            case "show_id" :
                return ResponseEntity.ok(id);
            // 비밀번호 변경
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
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // 조회된 Member 객체에서 정보 추출
        Member currentMember = member.get();
        // 현재 로그인한 사용자 id로 선호 카테고리 검색
        List<UserFavoriteCategory> userFavoriteCategories = memberService.findByMemberId(currentMember);

        // 현재 사용자의 선호 카테고리 목록을 카테고리 id로 매핑하여 반환
        List<Long> favoriteCategoryNames = userFavoriteCategories.stream()
                // 각 UserFavoriteCategory 객체에서 Category 객체를 추출
                .map(UserFavoriteCategory::getCategory)
                // 각 Category 객체에서 카테고리 id 추출
                .map(Category::getId)
                // 카테고리 id 리스트로 반환
                .toList();

        return ResponseEntity.ok(favoriteCategoryNames.toString());
    }

    // 선호 카테고리 편집
    @PostMapping("mypage/edit-favorite-category")
    @Transactional
    public ResponseEntity<String> editFavoriteCategory(
            @RequestBody UserFavoriteCategoryDTO userFavoriteCategoryDTO) throws IOException {
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        // 조회된 Member 객체에서 정보 추출
        Member currentMember = member.get();

        // 기존 선호 카테고리 리스트 삭제
        userFavoriteCategoryRepository.deleteByMember(currentMember);

        // 선호 카테고리 편집 목록이 비어 있지 않은지 확인
        if (!userFavoriteCategoryDTO.getCategoryIds().isEmpty()) {
            // 선호 카테고리 리스트 생성
            // userFavoriteCategoryDTO 에서 가져온 카테고리 ID 리스트의 각 ID를 userFavoriteCategory 객체로 변환한 후 선호 카테고리 리스트에 저장
            List<UserFavoriteCategory> userFavoriteCategories = userFavoriteCategoryDTO.getCategoryIds().stream()
                    .map(categoryId -> {
                        UserFavoriteCategory userFavoriteCategory = new UserFavoriteCategory(); // userFavoriteCategory 객체 생성
                        userFavoriteCategory.setMember(member.get()); // 현재 로그인한 사용자를 설정
                        userFavoriteCategory.setCategory(categoryRepository.findById(categoryId).orElseThrow()); // 해당 카테고리 ID에 해당하는 Category 객체를 찾아 설정 (카테고리가 존재하지 않으면 예외를 던짐)
                        userFavoriteCategory.setId(new UserFavoriteCategoryId(member.get().getId(), categoryId)); // UserFavoriteCategory 객체에 복합 키 설정 (복합 키는 현재 사용자 ID와 카테고리 ID로 구성됨)
                        return userFavoriteCategory; // 생성된 선호 카테고리 반환
                    })
                    .toList(); // 선호 카테고리 리스트로 변환
            // 생성된 선호 카테고리 리스트를 DB에 저장
            memberService.saveAll(userFavoriteCategories);
        }
        return ResponseEntity.ok("선호 카테고리가 저장되었습니다.");
    }

    // 프로필 사진 변경
    @PostMapping("/mypage/update-profile-image")
    public ResponseEntity<?> updateProfileImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        // 조회된 Member 객체에서 정보 추출
        Member currentMember = member.get();

        // 성공적으로 변경된 프로필 이미지 정보 반환
        return ResponseEntity.ok(profileImageService.updateProfileImage(currentMember, file));
    }

    // 알람 설정
    @PostMapping("/mypage/update-alarm")
    public ResponseEntity<String> updateAlarm(@RequestBody String alarmRequest) throws IOException{
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        // 조회된 Member 객체에서 정보 추출
        Member currentMember = member.get();

        // JSON 데이터를 파싱하여 알람 설정 추출
        JsonNode jsonNode = jacksonObjectMapper.readTree(alarmRequest);
        boolean alarm = jsonNode.get("alarm").asBoolean();

        // 알람 설정 요청이 비어 있지 않은지 확인
        if (!alarmRequest.isEmpty()) {
            currentMember.setAlarm(alarm); // 알람 설정
        }
        memberRepository.save(currentMember); // 변경된 알람 설정을 DB에 저장
        return ResponseEntity.ok("알람 설정이 " +alarm+ "로 변경되었습니다.");
    }
}