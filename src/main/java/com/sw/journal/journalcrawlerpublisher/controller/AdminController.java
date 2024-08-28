package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.constant.Role;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.dto.ArticleModificationDTO;
import com.sw.journal.journalcrawlerpublisher.dto.BanDTO;
import com.sw.journal.journalcrawlerpublisher.dto.ReportDTO;
import com.sw.journal.journalcrawlerpublisher.exception.UnauthorizedException;
import com.sw.journal.journalcrawlerpublisher.service.AdminService;
import com.sw.journal.journalcrawlerpublisher.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*

[관리 api 위치]

(1)
-- 기사 관리 api : ArticleController/Service 에 구현
-- 댓글 관리 api : ReportController/Service 에 구현
(2)
-- adminController/Service 에 관리 api 구현

[관리자 인증로직]
(1)
-- /admin/** 경로로 접근 가능한 API 를 구현함
(2)
-- controller 마다 admin 인증 로직을 추가함

 */

// => 둘다 (2)로 구현함
// 필요시 추후 분리


@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final MemberService memberService;

    public AdminController(AdminService adminService,
                           MemberService memberService)
    {
        this.adminService = adminService;
        this.memberService = memberService;
    }


    //////////////////////////////


    // [댓글] - 신고된 댓글 목록 조회 --미사용
    @GetMapping("/report-list")
    public ResponseEntity<List<ReportDTO.Response>> getReportedComments() {
        // --관리자 권한 확인
        if (!isAdmin()) {
            throw new UnauthorizedException("관리자 권한이 필요합니다");
        }
        //
        List<ReportDTO.Response> reportedComments = adminService.getReportedComments();
        return ResponseEntity.ok(reportedComments);
    }

    // [댓글] - 신고된 댓글 목록 조회 (페이징)
    @GetMapping("/reports")
    public ResponseEntity<Page<ReportDTO.Response>> getReportedCommentsWithPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        // --관리자 권한 확인
        if (!isAdmin()) {
            throw new UnauthorizedException("관리자 권한이 필요합니다");
        }

        // 신고된 댓글 목록 조회 (페이징)
        Page<ReportDTO.Response> reportedComments = adminService.getReportedCommentsWithPaging(page, size, status);
        return ResponseEntity.ok(reportedComments);
    }

    // [댓글] - 특정 댓글 조회
    @GetMapping("/report/{reportId}")
    public ResponseEntity<ReportDTO.Response> getReportedComment(@PathVariable Long reportId) {
        // --관리자 권한 확인
        if (!isAdmin()) {
            throw new UnauthorizedException("관리자 권한이 필요합니다");
        }
        //
        ReportDTO.Response reportedComment = adminService.getReportedComment(reportId);
        return ResponseEntity.ok(reportedComment);
    }

    // [댓글] - 댓글 신고 상태 변경
    @PutMapping("/report/{reportId}")
    public ResponseEntity<ReportDTO.Response> updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody ReportDTO.Update updateStatus) {
        // --관리자 권한 확인
        if (!isAdmin()) {
            throw new UnauthorizedException("관리자 권한이 필요합니다");
        }
        //
        ReportDTO.Response updatedReport = adminService.updateReportStatus(reportId, updateStatus);
        return ResponseEntity.ok(updatedReport);
    }

    // [댓글] - 댓글 신고내역 삭제
    @DeleteMapping("/report/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long reportId) {
        // --관리자 권한 확인
        if (!isAdmin()) {
            throw new UnauthorizedException("관리자 권한이 필요합니다");
        }
        //
        adminService.deleteReport(reportId);
        return ResponseEntity.ok().build();
    }

    //////////////////////////////


    // [유저] - Ban 기록 조회
    @GetMapping("/user/ban-list")
    public ResponseEntity<List<BanDTO.Response>> getBanList() {
        // --관리자 권한 확인
        if (!isAdmin()) {
            throw new UnauthorizedException("관리자 권한이 필요합니다");
        }
        //
        List<BanDTO.Response> banList = adminService.getBanList();
        return ResponseEntity.ok(banList);
    }

    // [유저] - 유저 Ban하기
    @PostMapping("/user/ban")
    public ResponseEntity<Void> banUser(@RequestBody BanDTO.Request banRequest) {
        // --관리자 권한 확인
        if (!isAdmin()) {
            throw new UnauthorizedException("관리자 권한이 필요합니다");
        }
        //
        adminService.banUser(banRequest);
        return ResponseEntity.ok().build();
    }


    //////////////////////////////


    // [기사] - 기사 수정 (제목&내용&태그)
    @PutMapping("/article/{articleId}")
    public ResponseEntity<Void> updateArticle(
            @PathVariable Long articleId,
            @RequestBody ArticleModificationDTO.Request articleRequest) {
        // --관리자 권한 확인
        if (!isAdmin()) {
            throw new UnauthorizedException("관리자 권한이 필요합니다");
        }
        //
        adminService.updateArticle(articleId, articleRequest);
        return ResponseEntity.ok().build();
    }

    // [기사] - 기사 삭제
    @DeleteMapping("/article/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long articleId) {
        // --관리자 권한 확인
        if (!isAdmin()) {
            throw new UnauthorizedException("관리자 권한이 필요합니다");
        }
        //
        adminService.deleteArticle(articleId);
        return ResponseEntity.ok().build();
    }

    // 사용자가 관리자인지 확인하는 메서드
    private boolean isAdmin() {

        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Member currentMember = memberService.findByUsername(currentUsername)
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다"));

        // 사용자의 권한(role)이 ADMIN 또는 SUPER_ADMIN 인지 확인
        return currentMember.getRole() == Role.ADMIN || currentMember.getRole() == Role.SUPER_ADMIN;
    }

}
